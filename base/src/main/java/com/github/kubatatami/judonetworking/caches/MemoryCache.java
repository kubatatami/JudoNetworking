package com.github.kubatatami.judonetworking.caches;

import com.github.kubatatami.judonetworking.internals.results.CacheResult;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 31.05.2013
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public interface MemoryCache {


    CacheResult get(int methodId, Object params[], int cacheLifeTime, int cacheSize);

    void put(int methodId, Object params[], Object object, int cacheSize);

    void clearCache();

    void clearCache(Method method);

    void clearCache(Method method, Object... params);

    void clearCache(int methodId);

    void clearCache(int methodId, Object... params);

    int getDebugFlags();

    void setDebugFlags(int debugFlags);


}
