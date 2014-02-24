package com.github.kubatatami.judonetworking;


/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 12:49
 */
public interface Endpoint {

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
     * Create batch request.
     *
     * @param apiInterface API interface class
     * @param batch        Batch callback
     * @return Batch thread useful for synchronized wait
     */
    public <T> void callInBatch(Class<T> apiInterface, Batch<T> batch);

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
     * @param delay
     */
    public void setDelay(int delay);

    /**
     * @param maxMobileConnections
     * @param maxWifiConnections
     */
    public void setMultiBatchConnections(int maxMobileConnections, int maxWifiConnections);

    /**
     * @param mode
     */
    public void setBatchTimeoutMode(BatchTimeoutMode mode);

    /**
     * Enables response cache.
     *
     * @param enabled If true cache is enable
     */
    public void setCacheEnabled(boolean enabled);


    public void setErrorLogger(ErrorLogger logger);

    /**
     * @param mode
     */
    public void setCacheMode(CacheMode mode);

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
    public void setClonner(Clonner clonner);


    public DiskCache getDiskCache();


    public MemoryCache getMemoryCache();

    public void setPercentLoss(float percentLoss);

    /**
     * @param onlyInDebugMode
     */
    public void startTest(boolean onlyInDebugMode, String name, int revision);

    /**
     *
     */
    public void stopTest();

    public ProtocolController getProtocolController();

    public void setVerifyResultModel(boolean enabled);

    public boolean isProcessingMethod();

    public void setProcessingMethod(boolean enabled);


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
     * Log request code line.
     */
    public static final int REQUEST_LINE_DEBUG = 16;

    /**
     * Log request and response headers.
     */
    public static final int HEADERS_DEBUG = 32;

    /**
     * Log token behavior
     */
    public static final int TOKEN_DEBUG = 64;

    /**
     * Log everything.
     */
    public static final int FULL_DEBUG = TIME_DEBUG | REQUEST_DEBUG | RESPONSE_DEBUG | CACHE_DEBUG | REQUEST_LINE_DEBUG | HEADERS_DEBUG;
}
