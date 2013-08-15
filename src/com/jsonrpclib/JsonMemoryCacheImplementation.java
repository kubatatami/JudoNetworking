package com.jsonrpclib;

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
class JsonMemoryCacheImplementation implements JsonMemoryCache {
    private int debugFlags;
    private Map<Method, LruCache<Integer, JsonCacheObject>> cache
            = Collections.synchronizedMap(new HashMap<Method, LruCache<Integer, JsonCacheObject>>());


    protected Context context;

    public JsonMemoryCacheImplementation(Context context) {
        this.context = context;
    }

    @Override
    public JsonCacheResult get(Method method, Object params[], int cacheLifeTime, int cacheSize) {
        JsonCacheResult result = new JsonCacheResult();
        Integer hash = Arrays.deepHashCode(params);
        if (cache.containsKey(method)) {
            JsonCacheObject cacheObject = cache.get(method).get(hash);
            if (cacheObject != null) {
                if (cacheLifeTime == 0 || System.currentTimeMillis() - cacheObject.createTime < cacheLifeTime) {
                    if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
                        JsonLoggerImpl.log("Cache(" + method + "): Get from memory cache.");
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
            cache.put(method, new LruCache<Integer, JsonCacheObject>(cacheSize));
        }
        Integer hash = Arrays.deepHashCode(params);
        cache.get(method).put(hash, new JsonCacheObject(System.currentTimeMillis(), object));
        if ((debugFlags & JsonRpc.CACHE_DEBUG) > 0) {
            JsonLoggerImpl.log("Cache(" + method + "): Saved in memory cache.");
        }
    }


    @Override
    public void clearCache() {
        cache = Collections.synchronizedMap(new HashMap<Method, LruCache<Integer, JsonCacheObject>>());

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
