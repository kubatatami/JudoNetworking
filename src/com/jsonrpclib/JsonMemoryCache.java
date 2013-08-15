package com.jsonrpclib;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 31.05.2013
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public interface JsonMemoryCache {


    public JsonCacheResult get(Method method, Object params[], int cacheLifeTime, int cacheSize);

    public void put(Method method, Object params[], Object object, int cacheSize);

    public void clearCache();

    public void clearCache(Method method);

    public void clearCache(Method method, Object... params);

    public int getDebugFlags();

    public void setDebugFlags(int debugFlags);


}
