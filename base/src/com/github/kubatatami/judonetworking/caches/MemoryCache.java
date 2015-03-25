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


    public CacheResult get(int methodId, Object params[], int cacheLifeTime, int cacheSize);

    public void put(int methodId, Object params[], Object object, int cacheSize);

    public void clearCache();

    public void clearCache(Method method);

    public void clearCache(Method method, Object... params);

    public void clearCache(int methodId);

    public void clearCache(int methodId, Object... params);

    public int getDebugFlags();

    public void setDebugFlags(int debugFlags);


}
