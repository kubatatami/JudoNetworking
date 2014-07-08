package com.github.kubatatami.judonetworking.transports;

import android.os.Build;

import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.ReflectionCache;
import com.github.kubatatami.judonetworking.RequestOutputStream;
import com.github.kubatatami.judonetworking.SecurityUtils;
import com.github.kubatatami.judonetworking.TimeStat;
import com.github.kubatatami.judonetworking.TransportLayer;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.HttpException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

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
import java.net.ConnectException;
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

import javax.net.ssl.HttpsURLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 29.03.2013
 * Time: 13:14
 */
public class HttpTransportLayer extends TransportLayer {

    private int reconnections = 2;
    private int connectTimeout = 7500;
    private int methodTimeout = 5000;
    private boolean followRedirection = true;
    private CookieManager cookieManager;
    private String authKey;
    private String username;
    private String password;
    private SecurityUtils.DigestAuth digestAuth;
    private HttpURLCreator httpURLCreator = null;
    private HttpURLConnectionModifier httpURLConnectionModifier;
    private static SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    public HttpTransportLayer() {
        init(new HttpURLCreatorImplementation(), null, false);
    }

    public HttpTransportLayer(HttpURLCreator httpURLCreator) {
        init(httpURLCreator, null, false);
    }

    public HttpTransportLayer(HttpURLCreator httpURLCreator, HttpURLConnectionModifier httpURLConnectionModifier) {
        init(httpURLCreator, httpURLConnectionModifier, false);
    }


    public HttpTransportLayer(boolean forceDisableKeepAlive) {
        init(new HttpURLCreatorImplementation(), null, forceDisableKeepAlive);
    }

    public HttpTransportLayer(HttpURLCreator httpURLCreator, boolean forceDisableKeepAlive) {
        init(httpURLCreator, null, forceDisableKeepAlive);
    }

    public HttpTransportLayer(HttpURLCreator httpURLCreator, HttpURLConnectionModifier httpURLConnectionModifier, boolean forceDisableKeepAlive) {
        init(httpURLCreator, httpURLConnectionModifier, forceDisableKeepAlive);
    }

    private void init(HttpURLCreator httpURLCreator, HttpURLConnectionModifier httpURLConnectionModifier, boolean forceDisableKeepAlive) {
        try {
            Field field = Class.forName("org.apache.harmony.luni.internal.net.www.protocol.jar.JarURLConnectionImpl").getDeclaredField("jarCache");
            field.setAccessible(true);
            HashMap map = (HashMap) field.get(null);
            map.clear();
        } catch (Exception e) {

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

    public void setDigestAuthentication(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public void setBasicAuthentication(final String username, final String password) {
        if(username == null || password == null){
            authKey=null;
        }else {
            authKey = SecurityUtils.getBasicAuthHeader(username, password);
        }
    }

    public void setBasicAuthentication(final String hash) {
        if(hash == null){
            authKey=null;
        }else {
            authKey = "Basic " + hash;
        }

    }

    public void setFollowRedirection(boolean followRedirection) {
        this.followRedirection = followRedirection;
    }


    protected HttpURLConnection createHttpUrlConnection(String url) throws Exception {
        HttpURLConnection urlConnection = null;
        for (int i = 1; i <= reconnections; i++) {
            try {
                urlConnection = httpURLCreator.create(url);
                break;
            } catch (IOException e) {
                if (i == reconnections) {
                    throw e;
                }
            }
        }
        return urlConnection;
    }

    protected void initSetup(HttpURLConnection urlConnection, ProtocolController.RequestInfo requestInfo,
                             int timeout, TimeStat timeStat, Method method, CacheInfo cacheInfo) throws Exception {
        if (urlConnection == null) {
            throw new ConnectException("Can't create HttpURLConnection.");
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
        if (method != null) {
            HttpMethod ann = ReflectionCache.getAnnotationInherited(method, HttpMethod.class);
            if(ann!=null) {
                urlConnection.setRequestMethod(ann.value());
            }
        }
        if (requestInfo.customHeaders != null) {
            for (Map.Entry<String, String> entry : requestInfo.customHeaders.entrySet()) {
                urlConnection.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void logRequestHeaders(int debugFlags, HttpURLConnection urlConnection) {
        if ((debugFlags & Endpoint.HEADERS_DEBUG) > 0) {
            String headers = "";
            for (String key : urlConnection.getRequestProperties().keySet()) {
                headers += key + ":" + urlConnection.getRequestProperty(key) + " ";
            }
            longLog("Request headers", headers);
        }

    }

    protected void logResponseHeaders(int debugFlags, HttpURLConnection urlConnection) {
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
    }


    protected void sendRequest(HttpURLConnection urlConnection, ProtocolController.RequestInfo requestInfo,
                               TimeStat timeStat, int debugFlags) throws Exception {
        OutputStream stream=null;
        try {
            if (digestAuth != null) {
                String digestHeader = SecurityUtils.getDigestAuthHeader(digestAuth, urlConnection.getURL(), requestInfo, username, password);
                if ((debugFlags & Endpoint.TOKEN_DEBUG) > 0) {
                    longLog("digest", digestHeader);
                }
                urlConnection.addRequestProperty("Authorization", digestHeader);
            }

            if (requestInfo.entity != null) {
                urlConnection.setDoOutput(true);
                if (!(urlConnection instanceof HttpsURLConnection)) {   //prevent android bug
                    urlConnection.setFixedLengthStreamingMode((int) requestInfo.entity.getContentLength());
                }
                stream = requestInfo.entity.getContentLength() > 0 ?
                        new RequestOutputStream(urlConnection.getOutputStream(), timeStat,
                                requestInfo.entity.getContentLength()) : urlConnection.getOutputStream();
                timeStat.tickConnectionTime();
                if ((debugFlags & Endpoint.REQUEST_DEBUG) > 0) {
                    longLog("Request(" + requestInfo.url + ")", convertStreamToString(requestInfo.entity.getContent()));
                    requestInfo.entity.reset();
                }
                requestInfo.entity.writeTo(stream);
                stream.flush();
            } else {
                if ((debugFlags & Endpoint.REQUEST_DEBUG) > 0) {
                    longLog("Request", requestInfo.url);
                }
                urlConnection.getInputStream();
                timeStat.tickConnectionTime();
                timeStat.tickSendTime();
            }
        }finally {
            if(stream!=null){
                stream.close();
            }
        }

    }

    public Connection send(final ProtocolController protocolController, ProtocolController.RequestInfo requestInfo,
                           int timeout, TimeStat timeStat, int debugFlags, Method method, CacheInfo cacheInfo) throws JudoException {
        boolean repeat = false;
        HttpURLConnection urlConnection = null;
        do {
            try {
                urlConnection = createHttpUrlConnection(requestInfo.url);
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                initSetup(urlConnection, requestInfo, timeout, timeStat, method, cacheInfo);
                logRequestHeaders(debugFlags, urlConnection);
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                sendRequest(urlConnection, requestInfo, timeStat, debugFlags);
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                logResponseHeaders(debugFlags, urlConnection);

                if ((debugFlags & Endpoint.RESPONSE_DEBUG) > 0) {
                    longLog("Response code", urlConnection.getResponseCode() + "");
                }
                return new FinalConnection(urlConnection, protocolController);
            } catch (FileNotFoundException ex) {
                int code;
                try {
                    code = urlConnection.getResponseCode();
                } catch (IOException e) {
                    throw new ConnectionException(e);
                }
                if (!repeat && username != null) {
                    digestAuth = SecurityUtils.handleDigestAuth(urlConnection, code);
                    repeat = (digestAuth != null);
                    if (!repeat) {
                        handleHttpException(protocolController, code, convertStreamToString(urlConnection.getErrorStream()));
                    }
                } else {
                    handleHttpException(protocolController, code, convertStreamToString(urlConnection.getErrorStream()));
                }
            } catch (Exception ex) {
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }
                if (!(ex instanceof JudoException)) {
                    throw new ConnectionException(ex);
                } else {
                    throw (JudoException) ex;
                }
            }

        } while (repeat);
        return null;
    }

    protected void handleHttpException(ProtocolController protocolController, int code, String message) throws JudoException {
        protocolController.parseError(code, message);
        throw new HttpException(message + "(" + code + ")", code);
    }

    class FinalConnection implements Connection {

        HttpURLConnection connection;
        ProtocolController protocolController;

        FinalConnection(HttpURLConnection connection, ProtocolController protocolController) {
            this.connection = connection;
            this.protocolController = protocolController;
        }

        @Override
        public InputStream getStream() throws ConnectionException {
            try {
                return connection.getInputStream();
            } catch (FileNotFoundException ex) {
                try {
                    handleHttpException(protocolController, connection.getResponseCode(), convertStreamToString(connection.getErrorStream()));
                } catch (IOException e) {
                    throw new ConnectionException(e);
                }
                return null;
            } catch (IOException e) {
                throw new ConnectionException(e);
            }

        }


        public boolean isNewestAvailable() throws ConnectionException {
            try {
                int code = connection.getResponseCode();
                return code != 304;
            } catch (IOException e) {
                throw new ConnectionException(e);
            }
        }

        public int getContentLength() {
            return connection.getContentLength();
        }

        public Map<String, List<String>> getHeaders() {
            return connection.getHeaderFields();
        }

        @Override
        public String getHash() {
            return connection.getHeaderField("ETag");
        }

        @Override
        public Long getDate() {
            String lastModified = connection.getHeaderField("Last-Modified");
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
            connection.disconnect();
        }
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
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface HttpMethod {
        String value();
    }

    class HttpURLCreatorImplementation implements HttpURLCreator {

        @Override
        public HttpURLConnection create(String url) throws IOException {

            return (HttpURLConnection) new URL(url).openConnection();
        }

    }

}
