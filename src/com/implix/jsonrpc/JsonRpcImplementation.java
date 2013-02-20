package com.implix.jsonrpc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Proxy;

class JsonRpcImplementation implements JsonRpc {

    private int maxBatchConnections =0;
    private boolean wifiOnly=true;
    private JsonConnection jsonConnection;
    private Handler handler = new Handler();
    private Gson parser;
    private String apiKey = null;
    private ExclusionStrategy exclusionStrategy = new SerializationExclusionStrategy();
    private String authKey = null;
    private Context context;

    public JsonRpcImplementation(Context context, String url) {
        this.jsonConnection = new JsonConnection(url, this);
        this.parser = new GsonBuilder().addSerializationExclusionStrategy(exclusionStrategy).create();
        this.context = context;
    }

    public JsonRpcImplementation(Context context, String url, GsonBuilder builder) {
        this.jsonConnection = new JsonConnection(url, this);
        this.parser = builder.addSerializationExclusionStrategy(exclusionStrategy).create();
        this.context = context;
    }

    public JsonRpcImplementation(Context context, String url, String apiKey) {
        this.jsonConnection = new JsonConnection(url, this);
        this.parser = new GsonBuilder().addSerializationExclusionStrategy(exclusionStrategy).create();
        this.apiKey = apiKey;
        this.context = context;
    }

    public JsonRpcImplementation(Context context, String url, String apiKey, GsonBuilder builder) {
        this.jsonConnection = new JsonConnection(url, this);
        this.parser = builder.addSerializationExclusionStrategy(exclusionStrategy).create();
        this.apiKey = apiKey;
        this.context = context;
    }

    private class SerializationExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {
            JsonSerializationExclude ann = f.getAnnotation(JsonSerializationExclude.class);
            return ann != null;
        }
    }

    public void setPasswordAuthentication(final String username, final String password) {
        authKey = auth(username, password);
    }

    @Override
    public void setJsonVersion(JsonRpcVersion version) {
        jsonConnection.setJsonVersion(version);
    }

    @Override
    public void setTimeouts(int connectionTimeout, int methodTimeout, int reconnectionAttempts) {
        jsonConnection.setConnectTimeout(connectionTimeout);
        jsonConnection.setMethodTimeout(methodTimeout);
        jsonConnection.setReconnections(reconnectionAttempts);
    }

    @Override
    public void setCallbackThread(boolean alwaysMainThread) {
        if (alwaysMainThread) {
            handler = new Handler(Looper.getMainLooper());
        } else {
            handler = new Handler();
        }
    }

    @Override
    public void setDebugFlags(int flags) {
        jsonConnection.setDebugFlags(flags);
    }

    @Override
    public void setMultiBatchConnections(int maxConnections, boolean wifiOnly) {
        this.maxBatchConnections=maxConnections;
        this.wifiOnly=wifiOnly;
        System.setProperty("http.maxConnections", maxConnections+1+"");
    }

    private String auth(String login, String pass) {
        String source = login + ":" + pass;
        return "Basic " + Base64.encodeToString(source.getBytes(), Base64.NO_WRAP);
    }

    public <T> T getService(Class<T> obj) {
        return getService(obj, new JsonProxy(context,this, false));
    }

    @SuppressWarnings("unchecked")
    private <T> T getService(Class<T> obj,  JsonProxy proxy) {
        return (T) Proxy.newProxyInstance(obj.getClassLoader(), new Class<?>[]{obj}, proxy);
    }

    @Override
    public <T> Thread callInBatch(Class<T> obj, JsonBatch<T> batch) {
        return callInBatch(obj, 0, batch);
    }

    @Override
    public <T> Thread callInBatch(Class<T> obj, final int timeout, final JsonBatch<T> batch) {

        final JsonProxy pr = new JsonProxy(context, this, true);
        T proxy = getService(obj, pr);
        batch.run(proxy);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                pr.callBatch(timeout, batch);
            }
        });
        thread.start();
        return thread;
    }

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Handler getHandler() {
        return handler;
    }

    public JsonConnection getJsonConnection() {
        return jsonConnection;
    }


    public Gson getParser() {
        return parser;
    }

    public String getAuthKey() {
        return authKey;
    }

    public int getMaxBatchConnections() {
        return maxBatchConnections;
    }

    public boolean isWifiOnly() {
        return wifiOnly;
    }

    public String getApiKey() {
        return apiKey;
    }
}
