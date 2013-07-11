package com.jsonrpclib;

import android.content.Context;
import android.support.v4.util.LruCache;

import java.io.File;
import java.lang.reflect.Method;
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
public interface JsonDiscCache {

    public JsonCacheResult get(JsonCacheMethod method, Object params[],int cacheLifeTime, int cacheSize);

    public void put(JsonCacheMethod method, Object params[], Object object,int cacheSize);

    public void clearCache();

    public void clearCache(JsonCacheMethod method);

    public void clearCache(JsonCacheMethod method, Object... params);

    public void clearTests();

    public void clearTest(String name);

    public int getDebugFlags();

    public void setDebugFlags(int debugFlags);



}
