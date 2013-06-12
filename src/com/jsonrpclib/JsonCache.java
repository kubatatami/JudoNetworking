package com.jsonrpclib;

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

    public Object get(String method, Object params[],int cacheLifeTime);

    public void put(String method, Object params[], Object object,int cacheSize);

    public void clearCache();

    public void clearCache(String method);

    public void clearCache(String method, Object... params);

}
