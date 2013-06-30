package com.jsonrpclib;

import android.content.Context;
import android.support.v4.util.LruCache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 31.05.2013
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public interface JsonCache {



    public abstract JsonCacheResult get(String method, Object params[],int cacheLifeTime, int cacheSize, boolean persist);

    public abstract void put(String method, Object params[], Object object,int cacheSize, boolean persist);

    public abstract void clearCache();

    public abstract void clearCache(String method);

    public abstract void clearCache(String method, Object... params);

    public abstract int getDebugFlags();

    public abstract void setDebugFlags(int debugFlags);

    public class JsonCacheResult
    {
        public Object object;
        public boolean result;

    }

}
