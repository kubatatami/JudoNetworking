package com.github.kubatatami.judonetworking.caches;

import com.github.kubatatami.judonetworking.internals.cache.CacheMethod;
import com.github.kubatatami.judonetworking.internals.results.CacheResult;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 31.05.2013
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public interface DiskCache {

    CacheResult get(CacheMethod method, String hash, int cacheLifeTime);

    void put(CacheMethod method, String hash, Object object, int maxSize, Map<String, List<String>> headers);

    void clearCache();

    void clearCache(CacheMethod method);

    void clearCache(CacheMethod method, Object... params);

    int getDebugFlags();

    void setDebugFlags(int debugFlags);


}
