package com.implix.jsonrpc;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Proxy;

class JsonRpcImplementation implements JsonRpc {
    int timeout = 10000;
    private JsonConnection jsonConnection;
    private Handler handler = new Handler();
    private Gson parser;
    private String apiKey = null;
    private ExclusionStrategy exclusionStrategy = new SerializationExclusionStrategy();
    private String authKey = null;

    public JsonRpcImplementation(String url) {
        this.jsonConnection = new JsonConnection(url, this);
        this.parser = new GsonBuilder().addSerializationExclusionStrategy(exclusionStrategy).create();
    }

    public JsonRpcImplementation(String url, GsonBuilder builder) {
        this.jsonConnection = new JsonConnection(url, this);
        this.parser = builder.addSerializationExclusionStrategy(exclusionStrategy).create();
    }

    public JsonRpcImplementation(String url, String apiKey) {
        this.jsonConnection = new JsonConnection(url, this);
        this.parser = new GsonBuilder().addSerializationExclusionStrategy(exclusionStrategy).create();
        this.apiKey = apiKey;
    }

    public JsonRpcImplementation(String url, String apiKey, GsonBuilder builder) {
        this.jsonConnection = new JsonConnection(url, this);
        this.parser = builder.addSerializationExclusionStrategy(exclusionStrategy).create();
        this.apiKey = apiKey;
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
    public void setTimeouts(int connectionTimeout, int methodTimeout, int reconnections) {
        jsonConnection.setConnectTimeout(connectionTimeout);
        jsonConnection.setMethodTimeout(methodTimeout);
        jsonConnection.setReconnections(reconnections);
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

    private String auth(String login, String pass) {
        String source = login + ":" + pass;
        return "Basic " + Base64.encodeToString(source.getBytes(), Base64.NO_WRAP);
    }

    public <T> T getService(Class<T> obj) {
        return getService(obj, new JsonProxy(this, apiKey, false));
    }

    @SuppressWarnings("unchecked")
    private <T> T getService(Class<T> obj,  JsonProxy proxy) {
        return (T) Proxy.newProxyInstance(obj.getClassLoader(), new Class<?>[]{obj}, proxy);
    }

    @Override
    public <T> void callInBatch(Class<T> obj, JsonBatch<T> batch) {
        callInBatch(obj, 0, false, batch);
    }

    @Override
    public <T> void callInBatch(Class<T> obj, int timeout, JsonBatch<T> batch) {
        callInBatch(obj, timeout, false, batch);
    }

    @Override
    public <T> void callInBatch(Class<T> obj, boolean wait, JsonBatch<T> batch) {
        callInBatch(obj, 0, wait, batch);
    }

    @Override
    public <T> void callInBatch(Class<T> obj, final int timeout, boolean wait, final JsonBatch<T> batch) {

        final JsonProxy pr = new JsonProxy(this, apiKey, true);
        T proxy = getService(obj, pr);
        batch.run(proxy);


        AsyncTask task = new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                pr.callBatch(timeout, batch);
                return null;
            }
        }.execute();


        if (wait) {
            try {
                task.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public Handler getHandler() {
        return handler;
    }

    public JsonConnection getJsonConnection() {
        return jsonConnection;
    }

    public int getTimeout() {
        return timeout;
    }

    public Gson getParser() {
        return parser;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAuthKey() {
        return authKey;
    }
}
