package com.jsonrpclib;


/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 12:49
 */
public interface JsonRpc {

    /**
     * @param type
     * @param virtualServer
     * @param minDelay
     * @param maxDelay
     * @param <T>
     */
    public <T> void registerVirtualServer(Class<T> type, T virtualServer, int minDelay, int maxDelay);

    /**
     * @param type
     * @param virtualServer
     * @param delay
     * @param <T>
     */
    public <T> void registerVirtualServer(Class<T> type, T virtualServer, int delay);

    /**
     * @param type
     * @param virtualServer
     * @param <T>
     */
    public <T> void registerVirtualServer(Class<T> type, T virtualServer);

    /**
     * @param type
     * @param <T>
     */
    public <T> void unregisterVirtualServer(Class<T> type);


    /**
     * Create API proxy for given interface.
     *
     * @param apiInterface API interface class
     * @return Api proxy object.
     */
    public <T> T getService(Class<T> apiInterface);

    /**
     * Create API proxy for given interface.
     *
     * @param apiInterface API interface class
     * @param autoBatch    Enable auto batch mode.
     * @return API proxy object.
     */
    public <T> T getService(Class<T> apiInterface, boolean autoBatch);

    /**
     * Create batch request.
     *
     * @param apiInterface API interface class
     * @param batch        Batch callback
     * @return Batch thread useful for synchronized wait
     */
    public <T> void callInBatch(Class<T> apiInterface, JsonBatch<T> batch);

    /**
     * @param connectionTimeout
     * @param methodTimeout
     * @param reconnectionAttempts
     */
    public void setTimeouts(int connectionTimeout, int methodTimeout, int reconnectionAttempts);

    /**
     * @param alwaysMainThread
     */
    public void setCallbackThread(boolean alwaysMainThread);

    /**
     * @param flags
     */
    public void setDebugFlags(int flags);

    /**
     * @param maxMobileConnections
     * @param maxWifiConnections
     */
    public void setMultiBatchConnections(int maxMobileConnections, int maxWifiConnections);

    /**
     * Set byte array serialization type.
     *
     * @param asBase64 If true byte array will be serialize to base64 otherwise it will be normal array
     */
    public void setByteArraySerializationType(boolean asBase64);

    /**
     * @param autoBatchTime
     */
    public void setAutoBatchTime(int autoBatchTime);

    /**
     * @param mode
     */
    public void setBatchTimeoutMode(JsonBatchTimeoutMode mode);

    /**
     * Enables response cache.
     *
     * @param enabled If true cache is enable
     */
    public void setCacheEnabled(boolean enabled);


    public void setErrorLogger(JsonErrorLogger logger);

    /**
     * @param mode
     */
    public void setCacheMode(JsonCacheMode mode);

    /**
     * Enables statistics collection and
     *
     * @param enabled
     */
    public void setTimeProfilerEnabled(boolean enabled);

    /**
     * @return
     */
    public int getMaxStatFileSize();


    /**
     * @param maxStatFileSize
     */
    public void setMaxStatFileSize(int maxStatFileSize);

    /**
     * Writes time statistics to log.
     */
    public void showTimeProfilerInfo();

    /**
     * Clears time statistics.
     */
    public void clearTimeProfilerStat();

    /**
     * @param clonner
     */
    public void setJsonClonner(JsonClonner clonner);


    public JsonDiscCache getDiscCache();


    public JsonMemoryCache getMemoryCache();

    public void setPercentLoss(float percentLoss);

    /**
     * @param onlyInDebugMode
     */
    public void startTest(boolean onlyInDebugMode, String name, int revision);

    /**
     *
     */
    public void stopTest();

    /**
     * Log time of requests.
     */
    public static final int TIME_DEBUG = 1;
    /**
     * Log request content.
     */
    public static final int REQUEST_DEBUG = 2;
    /**
     * Log response content.
     */
    public static final int RESPONSE_DEBUG = 4;
    /**
     * Log cache behavior.
     */
    public static final int CACHE_DEBUG = 8;
    /**
     * Log cache behavior.
     */
    public static final int REQUEST_LINE_DEBUG = 16;
    /**
     * Log everything.
     */
    public static final int FULL_DEBUG = TIME_DEBUG | REQUEST_DEBUG | RESPONSE_DEBUG | CACHE_DEBUG | REQUEST_LINE_DEBUG;
}
