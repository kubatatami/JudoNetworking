package com.github.kubatatami.judonetworking.transports;

import android.content.Context;

import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.exceptions.CancelException;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.executors.JudoExecutor;
import com.github.kubatatami.judonetworking.internals.stats.TimeStat;
import com.github.kubatatami.judonetworking.internals.wear.MessageUtils;
import com.github.kubatatami.judonetworking.internals.wear.WearRequest;
import com.github.kubatatami.judonetworking.internals.wear.WearResponse;
import com.github.kubatatami.judonetworking.logs.JudoLogger;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;
import com.github.kubatatami.judonetworking.utils.SecurityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuba on 16/11/14.
 */
public class WearHttpTransportLayer extends HttpTransportLayer {


    protected MessageUtils messageUtils;

    public WearHttpTransportLayer(Context context) {
        messageUtils = new MessageUtils(context);
    }

    protected void initSetup(WearRequest request, ProtocolController.RequestInfo requestInfo,
                             int timeout, TimeStat timeStat, CacheInfo cacheInfo) throws Exception {
        request.setFollowRedirects(followRedirection);
        if (cacheInfo != null) {
            if (cacheInfo.hash != null) {
                request.addHeader("If-None-Match", cacheInfo.hash);
            } else if (cacheInfo.time != null) {
                request.addHeader("If-Modified-Since", format.format(new Date(cacheInfo.time)));
            }
        }
        if (requestInfo.mimeType != null) {
            request.addHeader("Content-Type", requestInfo.mimeType);
        }
        if (authKey != null) {
            request.addHeader("Authorization", authKey);
        }
        request.setConnectTimeout(connectTimeout);

        if (timeout == 0) {
            timeout = methodTimeout;
        }
        timeStat.setTimeout(timeout);
        request.setReadTimeout(timeout);


        if (requestInfo.customHeaders != null) {
            for (Map.Entry<String, String> entry : requestInfo.customHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    protected WearResponse sendRequest(WearRequest request, final ProtocolController.RequestInfo requestInfo,
                                   final TimeStat timeStat, Method method, int debugFlags) throws Exception {
        String methodName = "GET";

        try {
            if (digestAuth != null) {
                String digestHeader = SecurityUtils.getDigestAuthHeader(digestAuth, new URL(requestInfo.url), requestInfo, username, password);
                if ((debugFlags & Endpoint.TOKEN_DEBUG) > 0) {
                    longLog("digest", digestHeader, JudoLogger.LogLevel.DEBUG);
                }
                request.addHeader("Authorization", digestHeader);
            }

            if (requestInfo.entity != null) {
                methodName = "POST";
                request.setMimeType(requestInfo.mimeType);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                requestInfo.entity.writeTo(outputStream);
                request.setBody(outputStream.toByteArray());
            }
            if ((debugFlags & Endpoint.REQUEST_DEBUG) > 0) {
                if (request.getBody() != null && requestInfo.entity != null) {
                    longLog("Request(" + requestInfo.url + ")", convertStreamToString(requestInfo.entity.getContent()), JudoLogger.LogLevel.INFO);
                    requestInfo.entity.reset();
                } else {
                    longLog("Request", requestInfo.url, JudoLogger.LogLevel.INFO);
                }
            }


            if (method != null) {
                HttpMethod ann = ReflectionCache.getAnnotationInherited(method, HttpMethod.class);
                if (ann != null) {
                    methodName = ann.value();
                }
            }
            request.setMethodName(methodName);

            if (Thread.currentThread() instanceof JudoExecutor.ConnectionThread) {
                JudoExecutor.ConnectionThread connectionThread = (JudoExecutor.ConnectionThread) Thread.currentThread();
                connectionThread.setCanceller(new JudoExecutor.ConnectionThread.Canceller() {
                    @Override
                    public void cancel() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //TODO
                            }
                        }).start();
                    }
                });
            }
            WearResponse response = makeCallAndGetResponse(request);

            timeStat.tickConnectionTime();
            if (request.getBody() != null) {
                timeStat.tickSendTime();
            }
            return response;
        } finally {

            if (requestInfo.entity != null) {
                requestInfo.entity.close();
            }
        }

    }


    protected WearResponse makeCallAndGetResponse(WearRequest request) throws IOException {
        try {
            int timeoutSum = request.getConnectTimeout() + request.getReadTimeout();
            return messageUtils.sendMessageAndReceive(request, timeoutSum, WearResponse.class);
        } catch (IOException ex) {
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
    }

    @Override
    public Connection send(String requestName, ProtocolController protocolController, ProtocolController.RequestInfo requestInfo, int timeout, TimeStat timeStat, int debugFlags, Method method, CacheInfo cacheInfo) throws JudoException {
        boolean repeat = false;
        final WearRequest request = new WearRequest();
        final WearResponse response;
        do {
            try {
                request.setUrl(requestInfo.url);
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                initSetup(request, requestInfo, timeout, timeStat, cacheInfo);

                logRequestHeaders(requestName, debugFlags, request);

                response = sendRequest(request, requestInfo, timeStat, method, debugFlags);

                logResponseHeaders(requestName, debugFlags, response);

                if (!response.isSuccessful() && response.getCode() != 0) {
                    int code = response.getCode();
                    String message = response.getMessage();
                    String body = new String(response.getBody());
                    if (!repeat && username != null) {
                        digestAuth = SecurityUtils.handleDigestAuth(response.getHeader("WWW-Authenticate"), code);
                        repeat = (digestAuth != null);
                        if (!repeat) {
                            handleHttpException(protocolController, code, message, body);
                        }
                    } else {
                        handleHttpException(protocolController, code, message, body);
                    }
                }

                if ((debugFlags & Endpoint.RESPONSE_DEBUG) > 0) {
                    longLog("Response code(" + requestName + ")", response.getCode() + "", JudoLogger.LogLevel.DEBUG);
                }
                return new Connection() {

                    InputStream stream;

                    @Override
                    public InputStream getStream() throws ConnectionException {
                        if (stream == null) {
                            stream = new ByteArrayInputStream(response.getBody());
                        }
                        return stream;
                    }

                    @Override
                    public int getContentLength() {
                        return response.getBody().length;
                    }

                    public boolean isNewestAvailable() throws ConnectionException {
                        return response.getCode() != 304;
                    }

                    public Map<String, List<String>> getHeaders() {
                        Map<String, List<String>> map = new HashMap<>();
                        for (String name : response.getHeaders().keySet()) {
                            map.put(name, response.getHeaders(name));
                        }
                        return map;
                    }

                    @Override
                    public String getHash() {
                        return response.getHeader("ETag");
                    }

                    @Override
                    public Long getDate() {
                        String lastModified = response.getHeader("Last-Modified");
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
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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

    protected void logResponseHeaders(String requestName, int debugFlags, WearResponse response) {
        if ((debugFlags & Endpoint.HEADERS_DEBUG) > 0) {
            String headers = "";
            for (String key : response.getHeaders().keySet()) {
                if (key != null) {
                    headers += key + ":" + response.getHeader(key) + " ";
                }
            }
            longLog("Response headers(" + requestName + ")", headers, JudoLogger.LogLevel.DEBUG);
        }
    }

    protected void logRequestHeaders(String requestName, int debugFlags, WearRequest request) {
        if ((debugFlags & Endpoint.HEADERS_DEBUG) > 0) {
            String headers = "";
            for (String key : request.getHeaders().keySet()) {
                headers += key + ":" + request.getHeaders().get(key) + " ";
            }
            longLog("Request headers(" + requestName + ")", headers, JudoLogger.LogLevel.DEBUG);
        }

    }

    public void setWearConnectionTimeout(long wearConnectionTimeout) {
        messageUtils.setConnectionTimeout(wearConnectionTimeout);
    }

    public void setWearSendTimeout(long wearSendTimeout) {
        messageUtils.setSendTimeout(wearSendTimeout);
    }

    public void setWearReadTimeout(long wearReadTimeout) {
        messageUtils.setReadTimeout(wearReadTimeout);
    }

    @Override
    public void setMaxConnections(int max) {

    }
}
