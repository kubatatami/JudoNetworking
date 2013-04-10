package com.implix.jsonrpc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import com.google.gson22.ExclusionStrategy;
import com.google.gson22.FieldAttributes;
import com.google.gson22.Gson;
import com.google.gson22.GsonBuilder;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class JsonRpcImplementation implements JsonRpc {

    private int maxMobileConnections = 1;
    private int maxWifiConnections = 1;
    private JsonConnector jsonConnector;
    private JsonConnection connection;
    private Handler handler = new Handler();
    private Gson parser;
    private String apiKey = null;
    private ExclusionStrategy exclusionStrategy = new SerializationExclusionStrategy();
    private String authKey = null;
    private Context context;
    private boolean byteArrayAsBase64 = false;
    private boolean cacheEnabled = false;
    private boolean timeProfiler = false;
    private int autoBatchTime = 20; //milliseconds
    private JsonBatchTimeoutMode timeoutMode = JsonBatchTimeoutMode.TIMEOUTS_SUM;
    private JsonCache cache = new JsonCache(this);
    private int debugFlags = 0;
    private Map<String, JsonStat> stats;
    private File statFile;

    public JsonRpcImplementation(Context context, String url) {
        init(context,url,null,new GsonBuilder());
    }

    public JsonRpcImplementation(Context context, String url, GsonBuilder builder) {
        init(context,url,null,builder);
    }

    public JsonRpcImplementation(Context context, String url, String apiKey) {
        init(context,url,apiKey,new GsonBuilder());
    }

    public JsonRpcImplementation(Context context, String url, String apiKey, GsonBuilder builder) {
        init(context,url,apiKey,builder);
    }

    private void init(Context context, String url, String apiKey, GsonBuilder builder)
    {
        this.connection=new JsonHttpUrlConnection(this);
        this.jsonConnector = new JsonConnector(url, this,connection);
        this.parser = builder.addSerializationExclusionStrategy(exclusionStrategy).create();
        this.apiKey = apiKey;
        this.context = context;
        statFile = new File(context.getCacheDir(), "stats");
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
        jsonConnector.setJsonVersion(version);
    }

    @Override
    public void setTimeouts(int connectionTimeout, int methodTimeout, int reconnectionAttempts) {
        jsonConnector.setConnectTimeout(connectionTimeout);
        jsonConnector.setMethodTimeout(methodTimeout);
        jsonConnector.setReconnections(reconnectionAttempts);
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
        this.maxMobileConnections = maxMobileConnections;
        this.maxWifiConnections = maxWifiConnections;
        int max = Math.max(maxMobileConnections, maxWifiConnections);
        connection.setMaxConnections(max);
    }

    private String auth(String login, String pass) {
        String source = login + ":" + pass;
        return "Basic " + Base64.encodeToString(source.getBytes(), Base64.NO_WRAP);
    }

    public <T> T getService(Class<T> obj) {
        return getService(obj, false);
    }

    public <T> T getService(Class<T> obj, boolean autoBatch) {
        return getService(obj, new JsonProxy(context, this, autoBatch ? JsonBatchMode.AUTO : JsonBatchMode.NONE));
    }

    @SuppressWarnings("unchecked")
    private <T> T getService(Class<T> obj, JsonProxy proxy) {
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
        this.byteArrayAsBase64 = asBase64;
    }

    public boolean isByteArrayAsBase64() {
        return byteArrayAsBase64;
    }

    public Handler getHandler() {
        return handler;
    }

    public JsonConnector getJsonConnector() {
        return jsonConnector;
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
        this.timeoutMode = mode;
    }

    @Override
    public void setCacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
    }

    @Override
    public void setTimeProfilerEnabled(boolean enabled) {
        this.timeProfiler = enabled;
    }

    @Override
    public void showTimeProfilerInfo() {
        if (stats != null) {
            for (Map.Entry<String, JsonStat> entry : stats.entrySet()) {
                JsonLoggerImpl.log(entry.getKey() + ":" + entry.getValue());
            }
        }
    }

    @Override
    public void clearTimeProfilerStat() {
        statFile.delete();
        stats=Collections.synchronizedMap(new HashMap<String, JsonStat>());
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

    public boolean isTimeProfiler() {
        return timeProfiler;
    }

    @SuppressWarnings("unchecked")
    public Map<String, JsonStat> getStats() {
        if (stats == null) {
            if(statFile.exists())
            {

                try {
                    FileInputStream fileStream = new FileInputStream(statFile);
                    ObjectInputStream os = new ObjectInputStream(fileStream);
                    stats = (Map<String, JsonStat>)os.readObject();
                    os.close();
                } catch (Exception e) {
                   JsonLoggerImpl.log(e);
                    stats = Collections.synchronizedMap(new HashMap<String, JsonStat>());
                }
            }
            else
            {
                stats = Collections.synchronizedMap(new HashMap<String, JsonStat>());
            }
        }
        return stats;
    }

    public void saveStat() {
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(statFile));
            os.writeObject(stats);
            os.flush();
            os.close();
        } catch (IOException e) {
            JsonLoggerImpl.log(e);
        }

    }


    public Context getContext() {
        return context;
    }
}
