package com.jsonrpclib;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 12:49
 * To change this template use File | Settings | File Templates.
 */
public interface JsonRpc {

    public <T> T getService(Class<T> obj);

    public <T> T getService(Class<T> obj, boolean autoBatch);

    public <T> Thread callInBatch(Class<T> obj, JsonBatch<T> batch);

    public void setPasswordAuthentication(final String username, final String password);

    public void setJsonVersion(JsonRpcVersion version);

    public void setTimeouts(int connectionTimeout, int methodTimeout, int reconnectionAttempts);

    public void setCallbackThread(boolean alwaysMainThread);

    public void setDebugFlags(int flags);

    public void setMultiBatchConnections(int maxMobileConnections, int maxWifiConnections);

    public void setApiKey(String apiKey);

    public void setByteArraySerializationType(boolean asBase64);

    public void setAutoBatchTime(int autoBatchTime);

    public void setBatchTimeoutMode(JsonBatchTimeoutMode mode);

    public void setCacheEnabled(boolean enabled);

    public void setTimeProfilerEnabled(boolean enabled);

    public void showTimeProfilerInfo();

    public void clearTimeProfilerStat();

    public void clearCache();

    public void clearCache(Method method);

    public static final int TIME_DEBUG = 1;
    public static final int REQUEST_DEBUG = 2;
    public static final int RESPONSE_DEBUG = 4;
    public static final int CACHE_DEBUG = 8;

    public static final int FULL_DEBUG = 15;
}
