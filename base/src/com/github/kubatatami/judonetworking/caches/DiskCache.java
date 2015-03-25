package com.github.kubatatami.judonetworking.caches;

import com.github.kubatatami.judonetworking.internals.cache.CacheMethod;
import com.github.kubatatami.judonetworking.internals.results.CacheResult;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 31.05.2013
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public interface DiskCache {

    public CacheResult get(CacheMethod method, String hash, int cacheLifeTime);

    public void put(CacheMethod method, String hash, Object object, int maxSize);

    public void clearCache();

    public void clearCache(CacheMethod method);

    public void clearCache(CacheMethod method, Object... params);

    public void clearTests();

    public void clearTest(String name);

    public int getDebugFlags();

    public void setDebugFlags(int debugFlags);


}
