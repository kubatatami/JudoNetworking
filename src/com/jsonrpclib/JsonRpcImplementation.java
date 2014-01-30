package com.jsonrpclib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class JsonRpcImplementation implements JsonRpc {

    private int maxMobileConnections = 1;
    private int maxWifiConnections = 2;
    private JsonConnector jsonConnector;
    private JsonConnection connection;
    private Handler handler = new Handler();
    private Context context;
    private boolean cacheEnabled = false;
    private JsonCacheMode cacheMode = JsonCacheMode.NORMAL;
    private boolean timeProfiler = false;
    private int autoBatchTime = 20; //milliseconds
    private JsonBatchTimeoutMode timeoutMode = JsonBatchTimeoutMode.TIMEOUTS_SUM;
    private JsonMemoryCache memoryCache;
    private JsonDiskCache diskCache;
    private int debugFlags = 0;
    private Map<String, JsonStat> stats;
    private File statFile;
    private float percentLoss;
    private int maxStatFileSize = 50; //KB
    private JsonErrorLogger errorLogger;
    private JsonClonner jsonClonner = new JsonClonnerImplementation();
    private boolean test = false;
    private String testName = null;
    private int testRevision = 0;
    private int delay = 0;
    private String url;
    private ProtocolController protocolController;
    private HashMap<Class, JsonVirtualServerInfo> virtualServers = new HashMap<Class, JsonVirtualServerInfo>();
    private boolean verifyResultModel = false;
    private boolean processingMethod = false;

    public JsonRpcImplementation(Context context, ProtocolController protocolController, JsonConnection connection, String url) {
        init(context, protocolController, connection, url);
    }


    private void init(Context context, ProtocolController protocolController, JsonConnection connection, String url) {
        this.connection = connection;
        this.jsonConnector = new JsonConnector(url, this, connection);
        this.context = context;
        this.protocolController = protocolController;
        this.url = url;
        statFile = new File(context.getCacheDir(), "stats");
        memoryCache = new JsonMemoryCacheImplementation(context);
        diskCache = new JsonDiskCacheImplementation(context);
    }

    public HashMap<Class, JsonVirtualServerInfo> getVirtualServers() {
        return virtualServers;
    }

    @Override
    public <T> void registerVirtualServer(Class<T> type, T virtualServer, int delay) {
        virtualServers.put(type, new JsonVirtualServerInfo(virtualServer, delay, delay));
    }

    @Override
    public <T> void registerVirtualServer(Class<T> type, T virtualServer) {
        virtualServers.put(type, new JsonVirtualServerInfo(virtualServer, 0, 0));
    }

    @Override
    public <T> void registerVirtualServer(Class<T> type, T virtualServer, int minDelay, int maxDelay) {
        virtualServers.put(type, new JsonVirtualServerInfo(virtualServer, minDelay, maxDelay));
    }


    @Override
    public <T> void unregisterVirtualServer(Class<T> type) {
        virtualServers.remove(type);
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

    @Override
    public boolean isProcessingMethod() {
        return processingMethod;
    }

    @Override
    public void setProcessingMethod(boolean enabled) {
        this.processingMethod = enabled;
    }

    public <T> T getService(Class<T> obj) {
        return getService(obj, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> obj, boolean autoBatch) {
        return getService(obj, new JsonProxy(this, autoBatch ? JsonBatchMode.AUTO : JsonBatchMode.NONE));
    }

    @SuppressWarnings("unchecked")
    private <T> T getService(Class<T> obj, JsonProxy proxy) {
        return (T) Proxy.newProxyInstance(obj.getClassLoader(), new Class<?>[]{obj}, proxy);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void callInBatch(Class<T> obj, final JsonBatch<T> batch) {

        if ((getDebugFlags() & JsonRpc.REQUEST_LINE_DEBUG) > 0) {
            StackTraceElement stackTraceElement = JsonProxy.getExternalStacktrace(Thread.currentThread().getStackTrace());
            JsonLoggerImpl.log("Batch starts from " +
                    stackTraceElement.getClassName() +
                    "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
        }

        final JsonProxy pr = new JsonProxy(this, JsonBatchMode.MANUAL);
        T proxy = getService(obj, pr);
        pr.setBatchFatal(true);
        batch.run(proxy);
        pr.setBatchFatal(false);
        batch.runNonFatal(proxy);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                pr.callBatch(batch);
            }
        });
        thread.start();

    }


    public Handler getHandler() {
        return handler;
    }

    public JsonConnector getJsonConnector() {
        return jsonConnector;
    }


    public int getMaxMobileConnections() {
        return maxMobileConnections;
    }

    public int getMaxWifiConnections() {
        return maxWifiConnections;
    }

    public int getMaxConnections() {
        return JsonNetworkUtils.isWifi(context) ? getMaxWifiConnections() : getMaxMobileConnections();
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
    public void setErrorLogger(JsonErrorLogger logger) {
        this.errorLogger = logger;
    }

    @Override
    public void setCacheMode(JsonCacheMode mode) {
        this.cacheMode = mode;
    }

    @Override
    public void setTimeProfilerEnabled(boolean enabled) {
        this.timeProfiler = enabled;
    }

    public int getMaxStatFileSize() {
        return maxStatFileSize;
    }

    public void setMaxStatFileSize(int maxStatFileSize) {
        this.maxStatFileSize = maxStatFileSize;
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
        boolean result = statFile.delete();
        stats = Collections.synchronizedMap(new HashMap<String, JsonStat>());
    }


    @Override
    public void startTest(boolean onlyInDebugMode, String name, int revision) {

        String className = context.getApplicationContext().getPackageName() + ".BuildConfig";
        try {
            Class<?> clazz = Class.forName(className);

            Field field = clazz.getDeclaredField("DEBUG");

            Boolean debug = (Boolean) field.get(null);

            if (!onlyInDebugMode || debug) {
                this.test = true;
                this.testName = name;
                this.testRevision = revision;
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void setVerifyResultModel(boolean enabled) {
        verifyResultModel = enabled;
    }

    boolean isVerifyResultModel() {
        return verifyResultModel;
    }

    String getTestName() {
        return testName;
    }

    int getTestRevision() {
        return testRevision;
    }

    @Override
    public void stopTest() {
        this.test = false;
    }

    public JsonBatchTimeoutMode getTimeoutMode() {
        return timeoutMode;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }


    void setMemoryCache(JsonMemoryCache memoryCache) {
        this.memoryCache = memoryCache;
    }


    public JsonDiskCache getDiskCache() {
        return diskCache;
    }


    public JsonMemoryCache getMemoryCache() {
        return memoryCache;
    }

    @Override
    public void setPercentLoss(float percentLoss) {
        this.percentLoss = percentLoss;
    }

    @Override
    public void setDebugFlags(int flags) {
        this.debugFlags = flags;
        if (memoryCache != null) {
            memoryCache.setDebugFlags(flags);
        }

        if (diskCache != null) {
            diskCache.setDebugFlags(flags);
        }
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
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
            if (statFile.exists() && statFile.length() < maxStatFileSize * 1024) {

                try {
                    FileInputStream fileStream = new FileInputStream(statFile);
                    ObjectInputStream os = new ObjectInputStream(fileStream);
                    stats = (Map<String, JsonStat>) os.readObject();
                    os.close();
                } catch (Exception e) {
                    JsonLoggerImpl.log(e);
                    stats = Collections.synchronizedMap(new HashMap<String, JsonStat>());
                }
            } else {
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

    JsonCacheMode getCacheMode() {
        return cacheMode;
    }

    public Context getContext() {
        return context;
    }

    public JsonErrorLogger getErrorLogger() {
        return errorLogger;
    }

    public JsonClonner getJsonClonner() {
        return jsonClonner;
    }

    public void setJsonClonner(JsonClonner clonner) {
        this.jsonClonner = clonner;
    }

    public String getUrl() {
        return url;
    }

    boolean isTest() {
        return test;
    }

    public ProtocolController getProtocolController() {
        return protocolController;
    }

    public float getPercentLoss() {
        return percentLoss;
    }
}
