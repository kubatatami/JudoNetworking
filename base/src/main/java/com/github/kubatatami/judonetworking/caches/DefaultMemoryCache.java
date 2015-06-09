package com.github.kubatatami.judonetworking.caches;

import android.content.Context;
import android.support.v4.util.LruCache;

import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.internals.cache.CacheMethod;
import com.github.kubatatami.judonetworking.internals.results.CacheResult;
import com.github.kubatatami.judonetworking.logs.JudoLogger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.03.2013
 * Time: 08:05
 */
public class DefaultMemoryCache implements MemoryCache {
    private int debugFlags;
    private Map<Integer, LruCache<Integer, CacheObject>> cache
            = Collections.synchronizedMap(new HashMap<Integer, LruCache<Integer, CacheObject>>());


    protected Context context;

    public DefaultMemoryCache(Context context) {
        this.context = context;
    }

    @Override
    public CacheResult get(int methodId, Object params[], int cacheLifeTime, int cacheSize) {
        CacheResult result = new CacheResult();
        Integer hash = Arrays.deepHashCode(params);
        if (cache.containsKey(methodId)) {
            if ((debugFlags & Endpoint.CACHE_DEBUG) > 0) {
                JudoLogger.log("Search for " + methodId + " with hash:" + hash);
            }
            CacheObject cacheObject = cache.get(methodId).get(hash);
            if (cacheObject != null) {
                if (cacheLifeTime == 0 || System.currentTimeMillis() - cacheObject.createTime < cacheLifeTime) {
                    if ((debugFlags & Endpoint.CACHE_DEBUG) > 0) {
                        JudoLogger.log("Cache(" + methodId + "): Get from memory cache object with hash:" + hash);
                    }
                    result.object = cacheObject.getObject();
                    result.time = cacheObject.createTime;
                    result.result = true;
                    return result;
                }
            }
        }

        return result;
    }

    @Override
    public void put(int methodId, Object params[], Object object, int cacheSize) {
        if (!cache.containsKey(methodId)) {
            cache.put(methodId, new LruCache<Integer, CacheObject>(cacheSize != 0 ? cacheSize : Integer.MAX_VALUE));
        }
        Integer hash = Arrays.deepHashCode(params);
        cache.get(methodId).put(hash, new CacheObject(System.currentTimeMillis(), object));
        if ((debugFlags & Endpoint.CACHE_DEBUG) > 0) {
            JudoLogger.log("Cache(" + methodId + "): Saved in memory cache with hash:" + hash);
        }
    }


    @Override
    public void clearCache() {
        cache = Collections.synchronizedMap(new HashMap<Integer, LruCache<Integer, CacheObject>>());

    }

    @Override
    public void clearCache(Method method) {
        int hash = CacheMethod.getMethodId(method);
        clearCache(hash);
    }

    @Override
    public void clearCache(Method method, Object... params) {
        int hash = CacheMethod.getMethodId(method);
        clearCache(hash, params);
    }

    @Override
    public void clearCache(int methodId) {
        if (cache.containsKey(methodId)) {
            cache.remove(methodId);
        }
    }

    @Override
    public void clearCache(int methodId, Object... params) {
        if (cache.containsKey(methodId)) {
            Integer paramHash = Arrays.deepHashCode(params);
            if (cache.get(methodId).get(paramHash) != null) {
                cache.get(methodId).remove(paramHash);
            }
        }
    }

    @Override
    public int getDebugFlags() {
        return debugFlags;
    }

    @Override
    public void setDebugFlags(int debugFlags) {
        this.debugFlags = debugFlags;
    }

    /**
     * Created with IntelliJ IDEA.
     * User: jbogacki
     * Date: 07.03.2013
     * Time: 07:55
     */
    static class CacheObject {
        final long createTime;
        private final Object object;

        CacheObject(long createTime, Object object) {
            this.createTime = createTime;
            this.object = object;
        }

        public long getCreateTime() {
            return createTime;
        }

        public Object getObject() {
            return object;
        }
    }
}
