package com.github.kubatatami.judonetworking;


import com.github.kubatatami.judonetworking.annotations.LocalCache;
import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.builders.BatchBuilder;
import com.github.kubatatami.judonetworking.caches.DiskCache;
import com.github.kubatatami.judonetworking.caches.MemoryCache;
import com.github.kubatatami.judonetworking.clonners.Clonner;
import com.github.kubatatami.judonetworking.internals.EndpointBase;
import com.github.kubatatami.judonetworking.internals.stats.MethodStat;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 12:49
 */
public interface Endpoint extends EndpointBase {

    /**
     * @param type
     * @param virtualServer
     * @param minDelay
     * @param maxDelay
     * @param <T>
     */
    <T> void registerVirtualServer(Class<T> type, T virtualServer, int minDelay, int maxDelay);

    /**
     * @param type
     * @param virtualServer
     * @param delay
     * @param <T>
     */
    <T> void registerVirtualServer(Class<T> type, T virtualServer, int delay);

    /**
     * @param type
     * @param virtualServer
     * @param <T>
     */
    <T> void registerVirtualServer(Class<T> type, T virtualServer);

    /**
     * @param type
     * @param <T>
     */
    <T> void unregisterVirtualServer(Class<T> type);


    /**
     * Create API proxy for given interface.
     *
     * @param apiInterface API interface class
     * @return Api proxy object.
     */
    <T> T getService(Class<T> apiInterface);

    /**
     * Create batch request.
     *
     * @param apiInterface API interface class
     * @param batch        Batch callback
     * @return Batch thread useful for synchronized wait
     */
    <T> AsyncResult callInBatch(Class<T> apiInterface, Batch<T> batch);

    /**
     * Create batch request.
     *
     * @param builder        Batch builder
     * @return Batch thread useful for synchronized wait
     */
    <T> AsyncResult callInBatch(final Class<T> apiInterface, final BatchBuilder<T> builder);

    /**
     * Create batch request.
     *
     * @param apiInterface API interface class
     * @param batch        Batch callback
     * @return Batch thread useful for synchronized wait
     */
    <T> AsyncResult callAsyncInBatch(final Class<T> apiInterface, final Batch<T> batch);

    /**
     * Create batch request.
     *
     * @param builder        Batch builder
     * @return Batch thread useful for synchronized wait
     */
    <T> AsyncResult callAsyncInBatch(final Class<T> apiInterface, final BatchBuilder<T> builder);

    /**
     * @param mode
     */
    void setBatchTimeoutMode(BatchTimeoutMode mode);

    /**
     * Enables response cache.
     *
     * @param enabled If true cache is enable
     */
    void setCacheEnabled(boolean enabled);


    /**
     * @param mode
     */
    void setCacheMode(CacheMode mode);

    /**
     * Enables statistics collection and
     *
     * @param enabled
     */
    void setTimeProfilerEnabled(boolean enabled);

    /**
     * @return Max stats file size
     */
    int getMaxStatFileSize();


    /**
     * @param maxStatFileSize
     */
    void setMaxStatFileSize(int maxStatFileSize);

    /**
     * Writes time statistics to log.
     */
    void showTimeProfilerInfo();

    /**
     * Clears time statistics.
     */
    void clearTimeProfilerStat();

    /**
     * @param clonner
     */
    void setClonner(Clonner clonner);


    DiskCache getDiskCache();


    MemoryCache getMemoryCache();

    void setMemoryCache(MemoryCache memoryCache);

    void setDiskCache(DiskCache diskCache);

    void clearCache();

    void setUrl(String url);

    String getUrl();

    int getDefaultMethodCacheLifeTime();

    void setDefaultMethodCacheLifeTime(int millis);

    int getDefaultMethodCacheSize();

    void setDefaultMethodCacheSize(int millis);

    LocalCache.CacheLevel getDefaultMethodCacheLevel();

    void setDefaultMethodCacheLevel(LocalCache.CacheLevel level);

    LocalCache.OnlyOnError getDefaultMethodCacheOnlyOnErrorMode();

    void setDefaultMethodCacheOnlyOnErrorMode(LocalCache.OnlyOnError onlyOnError);

    Map<String, MethodStat> getTimeProfilerStats();

    /**
     * Created with IntelliJ IDEA.
     * User: jbogacki
     * Date: 04.03.2013
     * Time: 21:21
     */
    enum BatchTimeoutMode {
        TIMEOUTS_SUM, LONGEST_TIMEOUT
    }

    /**
     * Created with IntelliJ IDEA.
     * User: jbogacki
     * Date: 28.05.2013
     * Time: 14:50
     * To change this template use File | Settings | File Templates.
     */
    enum CacheMode {
        NORMAL, CLONE
    }
}
