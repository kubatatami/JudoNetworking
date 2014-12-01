package com.github.kubatatami.judonetworking.transports;

import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.JudoExecutor;
import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.ReflectionCache;
import com.github.kubatatami.judonetworking.RequestOutputStream;
import com.github.kubatatami.judonetworking.SecurityUtils;
import com.github.kubatatami.judonetworking.TimeStat;
import com.github.kubatatami.judonetworking.TransportLayer;
import com.github.kubatatami.judonetworking.exceptions.CancelException;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import okio.BufferedSink;

/**
 * Created by Kuba on 16/11/14.
 */
public class OkHttpTransportLayer extends HttpTransportLayer {

    protected OkHttpConnectionModifier okHttpConnectionModifier;
    protected OkHttpClient baseClient = new OkHttpClient();

    protected void initSetup(OkHttpClient client, Request.Builder builder, ProtocolController.RequestInfo requestInfo,
                             int timeout, TimeStat timeStat, CacheInfo cacheInfo) throws Exception {

        client.setFollowSslRedirects(followRedirection);
        if (cacheInfo != null) {
            if (cacheInfo.hash != null) {
                builder.addHeader("If-None-Match", cacheInfo.hash);
            } else if (cacheInfo.time != null) {
                builder.addHeader("If-Modified-Since", format.format(new Date(cacheInfo.time)));
            }
        }
        if (requestInfo.mimeType != null) {
            builder.addHeader("Content-Type", requestInfo.mimeType);
        }
        if (authKey != null) {
            builder.addHeader("Authorization", authKey);
        }
        client.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS);

        if (timeout == 0) {
            timeout = methodTimeout;
        }
        timeStat.setTimeout(timeout);
        client.setReadTimeout(timeout, TimeUnit.MILLISECONDS);


        if (okHttpConnectionModifier != null) {
            okHttpConnectionModifier.modify(client, builder);
        }

        if (requestInfo.customHeaders != null) {
            for (Map.Entry<String, String> entry : requestInfo.customHeaders.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    protected Response sendRequest(OkHttpClient client, Request.Builder builder, final ProtocolController.RequestInfo requestInfo,
                                   final TimeStat timeStat, Method method, int debugFlags) throws Exception {
        RequestBody requestBody = null;
        String methodName = "GET";

        try {
            if (digestAuth != null) {
                String digestHeader = SecurityUtils.getDigestAuthHeader(digestAuth, new URL(requestInfo.url), requestInfo, username, password);
                if ((debugFlags & Endpoint.TOKEN_DEBUG) > 0) {
                    longLog("digest", digestHeader);
                }
                builder.addHeader("Authorization", digestHeader);
            }

            if (requestInfo.entity != null) {
                methodName = "POST";
                requestBody = new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse(requestInfo.mimeType);
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        OutputStream stream = requestInfo.entity.getContentLength() > 0 ?
                                new RequestOutputStream(sink.outputStream(), timeStat,
                                        requestInfo.entity.getContentLength()) : sink.outputStream();
                        requestInfo.entity.writeTo(stream);
                    }
                };
            }
            if ((debugFlags & Endpoint.REQUEST_DEBUG) > 0) {
                if (requestBody != null) {
                    longLog("Request(" + requestInfo.url + ")", convertStreamToString(requestInfo.entity.getContent()));
                    requestInfo.entity.reset();
                } else {
                    longLog("Request", requestInfo.url);
                }
            }


            if (method != null) {
                HttpMethod ann = ReflectionCache.getAnnotationInherited(method, HttpMethod.class);
                if (ann != null) {
                    methodName = ann.value();
                }
            }

            final Call call = client.newCall(builder.method(methodName, requestBody).build());
            JudoExecutor.ConnectionThread connectionThread = (JudoExecutor.ConnectionThread) Thread.currentThread();
            connectionThread.setCanceller(new JudoExecutor.ConnectionThread.Canceller() {
                @Override
                public void cancel() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            call.cancel();
                        }
                    }).start();
                }
            });
            Response response;
            try {
                response = call.execute();
            } catch (IOException ex) {
                JudoExecutor.ConnectionThread thread = (JudoExecutor.ConnectionThread) Thread.currentThread();
                if (thread.isCanceled()) {
                    thread.resetCanceled();
                    throw new CancelException();
                } else {
                    throw ex;
                }
            }
            timeStat.tickConnectionTime();
            if (requestBody != null) {
                timeStat.tickSendTime();
            }
            return response;
        } finally {

            if (requestInfo.entity != null) {
                requestInfo.entity.close();
            }
        }

    }

    @Override
    public Connection send(String requestName, ProtocolController protocolController, ProtocolController.RequestInfo requestInfo, int timeout, TimeStat timeStat, int debugFlags, Method method, CacheInfo cacheInfo) throws JudoException {
        boolean repeat = false;
        OkHttpBuilder builder = new OkHttpBuilder();
        OkHttpClient client = baseClient.clone();

        final Response response;
        do {
            try {
                builder.url(requestInfo.url);
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                initSetup(client, builder, requestInfo, timeout, timeStat, cacheInfo);

                logRequestHeaders(requestName,debugFlags, builder);

                response = sendRequest(client, builder, requestInfo, timeStat, method, debugFlags);

                logResponseHeaders(requestName, debugFlags, response);

                if (!response.isSuccessful() && response.code() != 0) {
                    int code = response.code();
                    String message = response.message();
                    if (!repeat && username != null) {
                        digestAuth = SecurityUtils.handleDigestAuth(response.header("WWW-Authenticate"), code);
                        repeat = (digestAuth != null);
                        if (!repeat) {
                            handleHttpException(protocolController, code, message);
                        }
                    } else {
                        handleHttpException(protocolController, code, message);
                    }
                }

                if ((debugFlags & Endpoint.RESPONSE_DEBUG) > 0) {
                    longLog("Response code(" + requestName + ")", response.code() + "");
                }
                return new Connection() {

                    @Override
                    public InputStream getStream() throws ConnectionException {
                        return response.body().byteStream();
                    }

                    @Override
                    public int getContentLength() {
                        return (int) response.body().contentLength();
                    }

                    public boolean isNewestAvailable() throws ConnectionException {
                        return response.code() != 304;
                    }

                    public Map<String, List<String>> getHeaders() {
                        Map<String, List<String>> map = new HashMap<String, List<String>>();
                        for (String name : response.headers().names()) {
                            map.put(name, response.headers(name));
                        }
                        return map;
                    }

                    @Override
                    public String getHash() {
                        return response.header("ETag");
                    }

                    @Override
                    public Long getDate() {
                        String lastModified = response.header("Last-Modified");
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

                    }
                };
            } catch (Exception ex) {
                if (!(ex instanceof JudoException)) {
                    throw new ConnectionException(ex);
                } else {
                    throw (JudoException) ex;
                }
            }

        } while (repeat);
    }

    protected void logResponseHeaders(String requestName, int debugFlags, Response response) {
        if ((debugFlags & Endpoint.HEADERS_DEBUG) > 0) {
            String headers = "";
            for (String key : response.headers().names()) {
                if (key != null) {
                    headers += key + ":" + response.header(key) + " ";
                }
            }
            longLog("Response headers(" + requestName + ")", headers);
        }
    }

    protected void logRequestHeaders(String requestName,int debugFlags, OkHttpBuilder builder) {
        if ((debugFlags & Endpoint.HEADERS_DEBUG) > 0) {
            String headers = "";
            for (String key : builder.headers.keySet()) {
                headers += key + ":" + builder.headers.get(key) + " ";
            }
            longLog("Request headers("+requestName+")", headers);
        }

    }

    @Override
    public void setMaxConnections(int max) {
        System.setProperty("http.maxConnections", max + "");
    }

    public void setOkHttpConnectionModifier(OkHttpConnectionModifier okHttpConnectionModifier) {
        this.okHttpConnectionModifier = okHttpConnectionModifier;
    }

    public interface OkHttpConnectionModifier {

        void modify(OkHttpClient client, Request.Builder builder);

    }

    static class  OkHttpBuilder extends Request.Builder{
        Map<String,String> headers=new HashMap<String, String>();

        @Override
        public Request.Builder addHeader(String name, String value) {
            headers.put(name, value);
            return super.addHeader(name, value);
        }
    }

}
