package com.github.kubatatami.judonetworking.internals.wear;

import android.content.pm.PackageManager;
import android.os.Process;

import com.github.kubatatami.judonetworking.logs.JudoLogger;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okio.BufferedSink;

public class WearListenerService extends WearableListenerService {

    protected MessageUtils messageUtils;

    protected OkHttpClient baseClient;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            messageUtils = new MessageUtils(this);
            baseClient = new OkHttpClient();
        }
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        if (messageEvent.getPath().contains(MessageUtils.MSG_PATH)) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
                String id = messageEvent.getPath().substring(MessageUtils.MSG_PATH.length());
                Object waitObject = MessageUtils.waitObjects.get(id);
                if (waitObject != null) {
                    MessageUtils.resultObjects.put(id, messageEvent.getData());
                    synchronized (waitObject) {
                        waitObject.notifyAll();
                    }
                }
            } else {
                final String id = messageEvent.getPath().substring(MessageUtils.MSG_PATH.length());
                DataLayerHttpTransportLayer.WearRequest request = null;
                try {
                    request = messageUtils.readObject(messageEvent.getData(), DataLayerHttpTransportLayer.WearRequest.class);
                } catch (IOException e) {
                    JudoLogger.log(e);
                    try {
                        messageUtils.sendMessage(id, messageEvent.getSourceNodeId(), new DataLayerHttpTransportLayer.WearResponse(e));
                    } catch (IOException e1) {
                        JudoLogger.log(e);
                    }
                }
                if (request != null) {
                    final DataLayerHttpTransportLayer.WearRequest finalRequest = request;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                            sendAndSendResponse(finalRequest, id, messageEvent.getSourceNodeId());
                        }
                    }).start();
                }
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    protected void sendAndSendResponse(DataLayerHttpTransportLayer.WearRequest request, String msgId, String nodeId) {
        DataLayerHttpTransportLayer.WearResponse response;
        try {
            response = send(request);
        } catch (IOException e) {
            response = new DataLayerHttpTransportLayer.WearResponse(e);
            JudoLogger.log(e);
        }
        try {
            messageUtils.sendMessage(msgId, nodeId, response);
        } catch (IOException e) {
            JudoLogger.log(e);
        }
    }

    protected DataLayerHttpTransportLayer.WearResponse send(final DataLayerHttpTransportLayer.WearRequest request) throws IOException {
        RequestBody requestBody = null;
        OkHttpClient client = baseClient.clone();
        client.setConnectTimeout(request.getConnectTimeout(), TimeUnit.MILLISECONDS);
        client.setReadTimeout(request.getReadTimeout(), TimeUnit.MILLISECONDS);
        Request.Builder builder = new Request.Builder();
        if (request.getBody() != null) {
            requestBody = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse(request.getMimeType());
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    sink.write(request.getBody());
                }

                @Override
                public long contentLength() throws IOException {
                    return request.getBody().length;
                }
            };
        }
        builder.method(request.getMethodName(), requestBody);
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        builder.url(request.getUrl());
        long realRequestTime = System.currentTimeMillis();
        Response response = client.newCall(builder.build()).execute();
        realRequestTime = System.currentTimeMillis() - realRequestTime;
        DataLayerHttpTransportLayer.WearResponse wearResponse = new DataLayerHttpTransportLayer.WearResponse();
        wearResponse.setSuccessful(response.isSuccessful());
        wearResponse.setCode(response.code());
        wearResponse.setMessage(response.message());
        wearResponse.setBody(response.body().bytes());
        Map<String, List<String>> map = new HashMap<>();
        for (String name : response.headers().names()) {
            map.put(name, response.headers(name));
        }
        wearResponse.setHeaders(map);
        wearResponse.setRealRequestTime(realRequestTime);
        return wearResponse;
    }
}