package com.github.kubatatami.judonetworking.transports;

import android.support.annotation.NonNull;

import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.exceptions.CancelException;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.executors.JudoExecutor;
import com.github.kubatatami.judonetworking.internals.stats.TimeStat;
import com.github.kubatatami.judonetworking.internals.streams.RequestOutputStream;
import com.github.kubatatami.judonetworking.logs.JudoLogger;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Created by Kuba on 16/11/14.
 */
public class OkHttpTransportLayer extends HttpTransportLayer {

    protected OkHttpConnectionModifier okHttpConnectionModifier;

    protected final OkHttpClient baseClient;

    public OkHttpTransportLayer() {
        this(new OkHttpClient());
    }

    public OkHttpTransportLayer(OkHttpClient baseClient) {
        this.baseClient = baseClient;
    }

    protected OkHttpClient initSetup(OkHttpClient.Builder clientBuilder, Request.Builder builder, ProtocolController.RequestInfo requestInfo,
                                     int timeout, TimeStat timeStat) throws Exception {
        clientBuilder.followRedirects(followRedirection).followSslRedirects(followRedirection);

        if (requestInfo.mimeType != null) {
            builder.addHeader("Content-Type", requestInfo.mimeType);
        }
        clientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);

        if (timeout == 0) {
            timeout = methodTimeout;
        }
        timeStat.setTimeout(timeout);
        clientBuilder.readTimeout(timeout, TimeUnit.MILLISECONDS);


        if (okHttpConnectionModifier != null) {
            okHttpConnectionModifier.modify(clientBuilder, builder);
        }

        if (requestInfo.customHeaders != null) {
            for (Map.Entry<String, String> entry : requestInfo.customHeaders.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return clientBuilder.build();
    }

    protected Response sendRequest(OkHttpClient client, Request.Builder builder, final ProtocolController.RequestInfo requestInfo,
                                   final TimeStat timeStat, Method method, int debugFlags) throws Exception {
        RequestBody requestBody = null;
        String methodName = "GET";
        try {
            if (requestInfo.entity != null) {
                methodName = "POST";
                requestBody = createRequestBody(requestInfo, timeStat);
            }
            logRequest(requestInfo, debugFlags, requestBody);
            methodName = changeHttpMethod(method, methodName);
            requestBody = createEmptyRequestBody(requestInfo, requestBody, methodName);
            final Call call = client.newCall(builder.method(methodName, requestBody).build());
            attachCanceller(call);
            return handleResponse(timeStat, requestBody, call);
        } finally {
            if (requestInfo.entity != null) {
                requestInfo.entity.close();
            }
        }
    }

    private Response handleResponse(TimeStat timeStat, RequestBody requestBody, Call call) throws IOException, InterruptedException {
        final OkHttpAsyncResult result = new OkHttpAsyncResult();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                synchronized (result) {
                    result.ex = e;
                    result.notify();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                synchronized (result) {
                    result.response = response;
                    result.notify();
                }
            }
        });
        synchronized (result) {
            result.wait();
        }
        if (result.ex != null) {
            checkThreadCancelled(result.ex);
        }
        timeStat.tickConnectionTime();
        if (requestBody != null) {
            timeStat.tickSendTime();
        }
        return result.response;
    }

    private static class OkHttpAsyncResult {

        Response response;

        IOException ex;
    }

    private void checkThreadCancelled(IOException ex) throws IOException {
        if (Thread.currentThread() instanceof JudoExecutor.ConnectionThread) {
            JudoExecutor.ConnectionThread thread = (JudoExecutor.ConnectionThread) Thread.currentThread();
            if (thread.isCanceled()) {
                thread.resetCanceled();
                throw new CancelException(thread.getName());
            } else {
                throw ex;
            }
        } else {
            throw ex;
        }
    }

    private void attachCanceller(final Call call) {
        if (Thread.currentThread() instanceof JudoExecutor.ConnectionThread) {
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
        }
    }

    @NonNull
    private RequestBody createRequestBody(final ProtocolController.RequestInfo requestInfo, final TimeStat timeStat) {
        RequestBody requestBody;
        requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(requestInfo.mimeType != null ? requestInfo.mimeType : "");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                OutputStream stream = requestInfo.entity.getContentLength() > 0 ?
                        new RequestOutputStream(sink.outputStream(), timeStat,
                                requestInfo.entity.getContentLength()) : sink.outputStream();
                requestInfo.entity.writeTo(stream);
            }

            @Override
            public long contentLength() throws IOException {
                return requestInfo.entity.getContentLength();
            }
        };
        return requestBody;
    }

    private RequestBody createEmptyRequestBody(final ProtocolController.RequestInfo requestInfo, RequestBody requestBody, String methodName) {
        if (okhttp3.internal.http.HttpMethod.requiresRequestBody(methodName) && requestBody == null) {
            requestBody = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse(requestInfo.mimeType != null ? requestInfo.mimeType : "");

                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {

                }

                @Override
                public long contentLength() throws IOException {
                    return 0;
                }
            };
        }
        return requestBody;
    }

    private String changeHttpMethod(Method method, String methodName) {
        if (method != null) {
            HttpMethod ann = ReflectionCache.getAnnotationInherited(method, HttpMethod.class);
            if (ann != null) {
                methodName = ann.value();
            }
        }
        return methodName;
    }

    private void logRequest(ProtocolController.RequestInfo requestInfo, int debugFlags, RequestBody requestBody) throws IOException {
        if ((debugFlags & Endpoint.REQUEST_DEBUG) > 0) {
            if (requestBody != null) {
                longLog("Request(" + requestInfo.url + ")", requestInfo.entity.getLog(), JudoLogger.LogLevel.INFO);
            } else {
                longLog("Request", requestInfo.url, JudoLogger.LogLevel.INFO);
            }
        }
    }

    @Override
    public Connection send(String requestName, ProtocolController protocolController, ProtocolController.RequestInfo requestInfo,
                           int timeout, TimeStat timeStat, int debugFlags, Method method) throws JudoException {
        try {
            OkHttpBuilder builder = new OkHttpBuilder();
            builder.url(requestInfo.url);
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            OkHttpClient client = initSetup(baseClient.newBuilder(), builder, requestInfo, timeout, timeStat);

            logRequestHeaders(requestName, debugFlags, builder);

            Response response = sendRequest(client, builder, requestInfo, timeStat, method, debugFlags);

            logResponseHeaders(requestName, debugFlags, response);

            if (!response.isSuccessful() && response.code() != 0) {
                int code = response.code();
                String message = response.message();
                String body = "";
                try {
                    body = response.body().string();
                } catch (IOException ignored) {
                }
                handleHttpException(protocolController, code, message, body);
            }

            if ((debugFlags & Endpoint.RESPONSE_DEBUG) > 0) {
                longLog("Response code(" + requestName + ")", response.code() + "", JudoLogger.LogLevel.DEBUG);
                longLog("Response protocol(" + requestName + ")", response.protocol().toString(), JudoLogger.LogLevel.DEBUG);
            }
            return new OkConnection(response);


        } catch (Exception ex) {
            if (!(ex instanceof JudoException)) {
                throw new ConnectionException(ex);
            } else {
                throw (JudoException) ex;
            }
        }
    }

    protected void logResponseHeaders(String requestName, int debugFlags, Response response) {
        if ((debugFlags & Endpoint.HEADERS_DEBUG) > 0) {
            String headers = "";
            for (String key : response.headers().names()) {
                if (key != null) {
                    headers += key + ":" + response.header(key) + " ";
                }
            }
            longLog("Response headers(" + requestName + ")", headers, JudoLogger.LogLevel.DEBUG);
        }
    }

    protected void logRequestHeaders(String requestName, int debugFlags, OkHttpBuilder builder) {
        if ((debugFlags & Endpoint.HEADERS_DEBUG) > 0) {
            String headers = "";
            for (String key : builder.headers.keySet()) {
                headers += key + ":" + builder.headers.get(key) + " ";
            }
            longLog("Request headers(" + requestName + ")", headers, JudoLogger.LogLevel.DEBUG);
        }

    }

    @Override
    public void setMaxConnections(int max) {
        baseClient.dispatcher().setMaxRequests(max);
    }

    public void setOkHttpConnectionModifier(OkHttpConnectionModifier okHttpConnectionModifier) {
        this.okHttpConnectionModifier = okHttpConnectionModifier;
    }

    public interface OkHttpConnectionModifier {

        void modify(OkHttpClient.Builder clientBuilder, Request.Builder builder);

    }

    static class OkHttpBuilder extends Request.Builder {

        Map<String, String> headers = new HashMap<>();

        @Override
        public Request.Builder addHeader(String name, String value) {
            headers.put(name, value);
            return super.addHeader(name, value);
        }
    }

    static class OkConnection implements Connection {

        protected Response response;

        public OkConnection(Response response) {
            this.response = response;
        }

        @Override
        public InputStream getStream() throws ConnectionException {
            return response.body().byteStream();
        }

        @Override
        public int getContentLength() {
            return (int) response.body().contentLength();
        }

        public Map<String, List<String>> getHeaders() {
            Map<String, List<String>> map = new HashMap<>();
            for (String name : response.headers().names()) {
                map.put(name, response.headers(name));
            }
            return map;
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
            response.close();
        }
    }

    public OkHttpClient getOkHttpClient() {
        return baseClient;
    }
}
