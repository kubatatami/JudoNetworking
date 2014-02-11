package com.judocallbacks;

import android.content.Context;
import android.support.v4.util.LruCache;

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
class MemoryCacheImplementation implements MemoryCache {
    private int debugFlags;
    private Map<Method, LruCache<Integer, CacheObject>> cache
            = Collections.synchronizedMap(new HashMap<Method, LruCache<Integer, CacheObject>>());


    protected Context context;

    public MemoryCacheImplementation(Context context) {
        this.context = context;
    }

    @Override
    public CacheResult get(Method method, Object params[], int cacheLifeTime, int cacheSize) {
        CacheResult result = new CacheResult();
        Integer hash = Arrays.deepHashCode(params);
        if (cache.containsKey(method)) {
            CacheObject cacheObject = cache.get(method).get(hash);
            if (cacheObject != null) {
                if (cacheLifeTime == 0 || System.currentTimeMillis() - cacheObject.createTime < cacheLifeTime) {
                    if ((debugFlags & Endpoint.CACHE_DEBUG) > 0) {
                        LoggerImpl.log("Cache(" + method + "): Get from memory cache.");
                    }
                    result.object = cacheObject.getObject();
                    result.result = true;
                    return result;
                }
            }
        }

        return result;
    }

    @Override
    public void put(Method method, Object params[], Object object, int cacheSize) {
        if (!cache.containsKey(method)) {
            cache.put(method, new LruCache<Integer, CacheObject>(cacheSize != 0 ? cacheSize : Integer.MAX_VALUE));
        }
        Integer hash = Arrays.deepHashCode(params);
        cache.get(method).put(hash, new CacheObject(System.currentTimeMillis(), object));
        if ((debugFlags & Endpoint.CACHE_DEBUG) > 0) {
            LoggerImpl.log("Cache(" + method + "): Saved in memory cache.");
        }
    }


    @Override
    public void clearCache() {
        cache = Collections.synchronizedMap(new HashMap<Method, LruCache<Integer, CacheObject>>());

    }

    @Override
    public void clearCache(Method method) {
        if (cache.containsKey(method)) {
            cache.remove(method);
        }


    }

    @Override
    public void clearCache(Method method, Object... params) {
        if (cache.containsKey(method)) {
            Integer hash = Arrays.deepHashCode(params);
            if (cache.get(method).get(hash) != null) {
                cache.get(method).remove(hash);
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

}
