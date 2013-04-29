package com.jsonrpclib;

import android.support.v4.util.LruCache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.03.2013
 * Time: 08:05
 *
 */
class JsonCache {
    private final JsonRpcImplementation rpc;
    private Map<String,LruCache<Integer, JsonCacheObject>> cache = Collections.synchronizedMap(new HashMap<String, LruCache<Integer, JsonCacheObject>>());

    JsonCache(JsonRpcImplementation rpc) {
        this.rpc = rpc;
    }

    public Object get(String method, Object params[],int cacheLifeTime)
    {
        if(cache.containsKey(method))
        {
            Integer hash= Arrays.deepHashCode(params);
            JsonCacheObject cacheObject= cache.get(method).get(hash);
            if(cacheObject!=null)
            {
                if(cacheLifeTime==0 || System.currentTimeMillis()-cacheObject.createTime<cacheLifeTime)
                {
                    if ((rpc.getDebugFlags() & JsonRpc.CACHE_DEBUG) > 0) {
                       JsonLoggerImpl.log("Cache("+method+"): Get from cache.");
                    }
                    return cacheObject.getObject();
                }
            }
        }
        return null;
    }

    public void put(String method, Object params[], Object object,int cacheSize)
    {
        if(!cache.containsKey(method))
        {
            cache.put(method,new LruCache<Integer, JsonCacheObject>(cacheSize));
        }
        Integer hash= Arrays.deepHashCode(params);
        cache.get(method).put(hash,new JsonCacheObject(System.currentTimeMillis(),object));
        if ((rpc.getDebugFlags() & JsonRpc.CACHE_DEBUG) > 0) {
            JsonLoggerImpl.log("Cache("+method+"): Saved in cache.");
        }
    }


    public void clearCache()
    {
        cache = Collections.synchronizedMap(new HashMap<String, LruCache<Integer, JsonCacheObject>>());
    }


    public void clearCache(String method)
    {
        if(cache.containsKey(method))
        {
            cache.remove(method);
        }
    }
}
