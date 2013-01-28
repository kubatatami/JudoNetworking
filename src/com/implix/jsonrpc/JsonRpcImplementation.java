package com.implix.jsonrpc;

import android.content.Context;
import android.os.Handler;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

class JsonRpcImplementation implements JsonRpc {
    int timeout = 10000;
    private Connection connection;
    private boolean transaction = false;
    private List<JsonRequest> requests = new ArrayList<JsonRequest>();
    private Handler handler = new Handler(); // new Handler(Looper.getMainLooper());
    private Gson parser;
    private String apiKey = null;
    private ExclusionStrategy exclusionStrategy = new SerializationExclusionStrategy();


    public JsonRpcImplementation(Context context, String url) {
        this.connection = new Connection(context, url, this);
        this.parser = new GsonBuilder().addSerializationExclusionStrategy(exclusionStrategy).create();
    }

    public JsonRpcImplementation(Context context, String url, GsonBuilder builder) {
        this.connection = new Connection(context, url, this);
        this.parser = builder.addSerializationExclusionStrategy(exclusionStrategy).create();
    }

    public JsonRpcImplementation(Context context, String url, String apiKey) {
        this.connection = new Connection(context, url, this);
        this.parser = new GsonBuilder().addSerializationExclusionStrategy(exclusionStrategy).create();
        this.apiKey = apiKey;
    }

    public JsonRpcImplementation(Context context, String url, String apiKey, GsonBuilder builder) {
        this.connection = new Connection(context, url, this);
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


    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> obj) {
        return (T) Proxy.newProxyInstance(obj.getClassLoader(), new Class<?>[]{obj}, new JsonProxy(this, apiKey));
    }

    @Override
    public void startTransaction(int timeout) {
        transaction = true;
        this.timeout = timeout;
    }

    @Override
    public void startTransaction() {
        transaction = true;
    }

    @Override
    public Thread endTransaction() {
        return endTransaction(null);
    }

    @Override
    public Thread endTransaction(final JsonTransactionCallback callback) {
        transaction = false;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (requests.size() > 0) {
                    connection.callBatch(requests, callback);
                }
            }
        });
        t.start();

        return t;
    }

    public void registerAsyncRequest(JsonRequest request) {
        requests.add(request);
    }

    public boolean isTransaction() {
        return transaction;
    }

    public Handler getHandler() {
        return handler;
    }

    public Connection getConnection() {
        return connection;
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
}
