package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 31.05.2013
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public interface JsonDiskCache {

    public JsonCacheResult get(JsonCacheMethod method, String hash, int cacheLifeTime);

    public void put(JsonCacheMethod method, String hash, Object object, int maxSize);

    public void clearCache();

    public void clearCache(JsonCacheMethod method);

    public void clearCache(JsonCacheMethod method, Object... params);

    public void clearTests();

    public void clearTest(String name);

    public int getDebugFlags();

    public void setDebugFlags(int debugFlags);


}
