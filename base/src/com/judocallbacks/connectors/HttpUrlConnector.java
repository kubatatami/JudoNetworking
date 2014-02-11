package com.judocallbacks.connectors;

import android.os.Build;
import android.util.Base64;
import com.judocallbacks.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 29.03.2013
 * Time: 13:14
 */
public class HttpUrlConnector extends Connector {

    private int reconnections = 3;
    private int connectTimeout = 15000;
    private int methodTimeout = 10000;
    private boolean followRedirection = true;
    private CookieManager cookieManager;
    private String authKey = null;
    private HttpURLCreator httpURLCreator = null;
    private HttpURLConnectionModifier httpURLConnectionModifier = null;
    private static SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    public HttpUrlConnector() {
        init(new HttpURLCreatorImplementation(), null, false);
    }

    public HttpUrlConnector(HttpURLCreator httpURLCreator) {
        init(httpURLCreator, null, false);
    }

    public HttpUrlConnector(HttpURLCreator httpURLCreator, HttpURLConnectionModifier httpURLConnectionModifier) {
        init(httpURLCreator, httpURLConnectionModifier, false);
    }


    public HttpUrlConnector(boolean forceDisableKeepAlive) {
        init(new HttpURLCreatorImplementation(), null, forceDisableKeepAlive);
    }

    public HttpUrlConnector(HttpURLCreator httpURLCreator, boolean forceDisableKeepAlive) {
        init(httpURLCreator, null, forceDisableKeepAlive);
    }

    public HttpUrlConnector(HttpURLCreator httpURLCreator, HttpURLConnectionModifier httpURLConnectionModifier, boolean forceDisableKeepAlive) {
        init(httpURLCreator, httpURLConnectionModifier, forceDisableKeepAlive);
    }

    private void init(HttpURLCreator httpURLCreator, HttpURLConnectionModifier httpURLConnectionModifier, boolean forceDisableKeepAlive) {
        try {
            Field field = Class.forName("org.apache.harmony.luni.internal.net.www.protocol.jar.JarURLConnectionImpl").getDeclaredField("jarCache");
            field.setAccessible(true);
            HashMap map = (HashMap) field.get(null);
            map.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.httpURLCreator = httpURLCreator;
        this.httpURLConnectionModifier = httpURLConnectionModifier;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);
        }
        disableConnectionReuseIfNecessary(forceDisableKeepAlive);
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    private void disableConnectionReuseIfNecessary(boolean forceDisableKeepAlive) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO || forceDisableKeepAlive) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    public void setHttpURLCreator(HttpURLCreator httpURLCreator) {
        this.httpURLCreator = httpURLCreator;
    }

    public void setHttpURLConnectionModifier(HttpURLConnectionModifier httpURLConnectionModifier) {
        this.httpURLConnectionModifier = httpURLConnectionModifier;
    }

    public void setBasicAuthentication(final String username, final String password) {
        authKey = auth(username, password);
    }

    public void setBasicAuthentication(final String hash) {
        authKey = "Basic " + hash;
    }

    public void setFollowRedirection(boolean followRedirection) {
        this.followRedirection = followRedirection;
    }

    private String auth(String login, String pass) {
        String source = login + ":" + pass;
        return "Basic " + Base64.encodeToString(source.getBytes(), Base64.NO_WRAP);
    }

    public Connection send(final ProtocolController protocolController, ProtocolController.RequestInfo requestInfo,
                           int timeout, TimeStat timeStat, int debugFlags, Method method, CacheInfo cacheInfo) throws Exception {

        HttpURLConnection urlConnection = null;

        for (int i = 1; i <= reconnections; i++) {
            try {
                urlConnection = httpURLCreator.create(requestInfo.url);
                break;
            } catch (IOException e) {
                if (i == reconnections) {
                    throw e;
                }
            }
        }

        if (urlConnection == null) {
            throw new RequestException("Can't create HttpURLConnection.");
        }

        urlConnection.setInstanceFollowRedirects(followRedirection);

        if (cacheInfo != null) {
            if (cacheInfo.hash != null) {
                urlConnection.addRequestProperty("If-None-Match", cacheInfo.hash);
            } else if (cacheInfo.time != null) {
                urlConnection.addRequestProperty("If-Modified-Since", format.format(new Date(cacheInfo.time)));
            }
        }

        if (requestInfo.mimeType != null) {
            urlConnection.addRequestProperty("Content-Type", requestInfo.mimeType);
        }

        if (authKey != null) {
            urlConnection.addRequestProperty("Authorization", authKey);
        }

        urlConnection.setConnectTimeout(connectTimeout);
        if (timeout == 0) {
            timeout = methodTimeout;
        }

        timeStat.setTimeout(timeout);
        urlConnection.setReadTimeout(timeout);

        if (httpURLConnectionModifier != null) {
            httpURLConnectionModifier.modify(urlConnection);
        }

        if (method != null && method.isAnnotationPresent(JsonHttpMethod.class)) {
            JsonHttpMethod httpMethod = method.getAnnotation(JsonHttpMethod.class);
            urlConnection.setRequestMethod(httpMethod.methodType());
        }

        if ((debugFlags & Endpoint.HEADERS_DEBUG) > 0) {
            String headers = "";
            for (String key : urlConnection.getRequestProperties().keySet()) {
                headers += key + ":" + urlConnection.getRequestProperty(key) + " ";
            }
            longLog("Request headers", headers);
        }
        if (requestInfo.entity != null) {
            urlConnection.setDoOutput(true);
            if (!(urlConnection instanceof HttpsURLConnection)) {   //prevent andoid bug
                urlConnection.setFixedLengthStreamingMode((int) requestInfo.entity.getContentLength());
            }
            OutputStream stream = requestInfo.entity.getContentLength() > 0 ?
                    new RequestOutputStream(urlConnection.getOutputStream(), timeStat, requestInfo.entity.getContentLength()) : urlConnection.getOutputStream();
            timeStat.tickConnectionTime();
            if ((debugFlags & Endpoint.REQUEST_DEBUG) > 0) {
                longLog("Request(" + requestInfo.url + ")", convertStreamToString(requestInfo.entity.getContent()));
                requestInfo.entity.reset();
            }
            requestInfo.entity.writeTo(stream);
            stream.flush();
            stream.close();
        } else {
            if ((debugFlags & Endpoint.REQUEST_DEBUG) > 0) {
                longLog("Request", requestInfo.url);
            }
            urlConnection.getInputStream();
            timeStat.tickConnectionTime();
            timeStat.tickSendTime();
        }

        if ((debugFlags & Endpoint.HEADERS_DEBUG) > 0) {
            String headers = "";
            if (urlConnection.getHeaderFields() != null) {
                for (String key : urlConnection.getHeaderFields().keySet()) {
                    if (key != null) {
                        headers += key + ":" + urlConnection.getHeaderField(key) + " ";
                    }
                }
            }
            longLog("Response headers", headers);
        }

        if ((debugFlags & Endpoint.RESPONSE_DEBUG) > 0) {
            longLog("Response code", urlConnection.getResponseCode() + "");
        }

        final HttpURLConnection finalConnection = urlConnection;
        return new Connection() {
            @Override
            public InputStream getStream() throws Exception {
                try {
                    return finalConnection.getInputStream();
                } catch (FileNotFoundException ex) {
                    int code = finalConnection.getResponseCode();
                    String resp = convertStreamToString(finalConnection.getErrorStream());
                    protocolController.parseError(code, resp);
                    throw new HttpException(resp + "(" + code + ")", code);
                }

            }

            public boolean isNewestAvailable() throws Exception {
                int code = finalConnection.getResponseCode();
                return code != 304;
            }

            public int getContentLength() {
                return finalConnection.getContentLength();
            }

            public Map<String, List<String>> getHeaders() {
                return finalConnection.getHeaderFields();
            }

            @Override
            public String getHash() {
                String etag = finalConnection.getHeaderField("ETag");
                return etag;
            }

            @Override
            public Long getDate() {
                String lastModified = finalConnection.getHeaderField("Last-Modified");
                if (lastModified != null) {

                    try {
                        Date date = format.parse(lastModified);
                        return date.getTime();
                    } catch (ParseException e) {
                        return null;
                    }

                } else {
                    return null;
                }
            }

            @Override
            public void close() {
                finalConnection.disconnect();
            }
        };

    }


    @Override
    public void setMaxConnections(int max) {
        System.setProperty("http.maxConnections", max + "");
    }


    public void setReconnections(int reconnections) {
        this.reconnections = reconnections;
    }


    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getMethodTimeout() {
        return methodTimeout;
    }

    public void setMethodTimeout(int methodTimeout) {
        this.methodTimeout = methodTimeout;
    }

    public interface HttpURLCreator {

        public HttpURLConnection create(String url) throws IOException;

    }

    public interface HttpURLConnectionModifier {

        void modify(HttpURLConnection connection);

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface JsonHttpMethod {
        String methodType();
    }

    class HttpURLCreatorImplementation implements HttpURLCreator {

        @Override
        public HttpURLConnection create(String url) throws IOException {

            return (HttpURLConnection) new URL(url).openConnection();
        }

    }

}
