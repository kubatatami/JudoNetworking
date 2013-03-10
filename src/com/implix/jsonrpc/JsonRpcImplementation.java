package com.implix.jsonrpc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.Base64;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class JsonRpcImplementation implements JsonRpc {

    private int maxMobileConnections=1;
    private int maxWifiConnections=1;
    private JsonConnection jsonConnection;
    private Handler handler = new Handler();
    private Gson parser;
    private String apiKey = null;
    private ExclusionStrategy exclusionStrategy = new SerializationExclusionStrategy();
    private String authKey = null;
    private Context context;
    private boolean byteArrayAsBase64=false;
    private boolean cacheEnabled=false;
    private int autoBatchTime=20; //milliseconds
    private JsonBatchTimeoutMode timeoutMode =JsonBatchTimeoutMode.TIMEOUTS_SUM;
    private JsonCache cache=new JsonCache(this);
    private int debugFlags = 0;

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
    public void setMultiBatchConnections(int maxMobileConnections, int maxWifiConnections) {
        this.maxMobileConnections=maxMobileConnections;
        this.maxWifiConnections=maxWifiConnections;
        int max = Math.max(maxMobileConnections,maxWifiConnections);
        String currentMaxConnections =System.getProperty("http.maxConnections");
        if(currentMaxConnections == null || Integer.parseInt(currentMaxConnections) < max*2)
        {
            System.setProperty("http.maxConnections", max*2+"");
        }
    }

    private String auth(String login, String pass) {
        String source = login + ":" + pass;
        return "Basic " + Base64.encodeToString(source.getBytes(), Base64.NO_WRAP);
    }

    public <T> T getService(Class<T> obj) {
        return getService(obj, false);
    }

    public <T> T getService(Class<T> obj, boolean autoBatch) {
        return getService(obj, new JsonProxy(context,this,autoBatch ? JsonBatchMode.AUTO : JsonBatchMode.NONE));
    }

    @SuppressWarnings("unchecked")
    private <T> T getService(Class<T> obj,  JsonProxy proxy) {
        return (T) Proxy.newProxyInstance(obj.getClassLoader(), new Class<?>[]{obj}, proxy);
    }

    @Override
    public <T> Thread callInBatch(Class<T> obj, final JsonBatch<T> batch) {

        final JsonProxy pr = new JsonProxy(context, this, JsonBatchMode.MANUAL);
        T proxy = getService(obj, pr);
        batch.run(proxy);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                pr.callBatch(batch);
            }
        });
        thread.start();
        return thread;
    }

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void setByteArraySerializationType(boolean asBase64) {
        this.byteArrayAsBase64=asBase64;
    }

    public boolean isByteArrayAsBase64() {
        return byteArrayAsBase64;
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


    public int getMaxMobileConnections() {
        return maxMobileConnections;
    }

    public int getMaxWifiConnections() {
        return maxWifiConnections;
    }

    public String getApiKey() {
        return apiKey;
    }

    public int getAutoBatchTime() {
        return autoBatchTime;
    }

    public void setAutoBatchTime(int autoBatchTime) {
        this.autoBatchTime = autoBatchTime;
    }

    @Override
    public void setBatchTimeoutMode(JsonBatchTimeoutMode mode) {
        this.timeoutMode =mode;
    }

    @Override
    public void setCacheEnabled(boolean enabled) {
       this.cacheEnabled=enabled;
    }

    @Override
    public void clearCache() {
        cache.clearCache();
    }

    @Override
    public void clearCache(Method method) {
        cache.clearCache(JsonProxy.getMethodName(method));
    }

    public JsonBatchTimeoutMode getTimeoutMode() {
        return timeoutMode;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public JsonCache getCache() {
        return cache;
    }

    @Override
    public void setDebugFlags(int flags) {
        this.debugFlags = flags;
    }

    public int getDebugFlags() {
        return debugFlags;
    }
}
