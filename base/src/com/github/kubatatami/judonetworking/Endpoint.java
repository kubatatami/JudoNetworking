package com.github.kubatatami.judonetworking;


/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 12:49
 */
public interface Endpoint extends EndpointBase{

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
    public <T> AsyncResult callInBatch(Class<T> apiInterface, Batch<T> batch);



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
     * @return Max stats file size
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

}
