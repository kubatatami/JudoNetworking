package com.jsonrpclib;

import java.lang.reflect.Method;

public class JsonCacheMethod {
    private String test;
    private int testRevision;
    private String url;
    private Method method;
    private boolean dynamic = false;
    private String hash;
    private Long time;
    private JsonLocalCacheLevel cacheLevel;

    public JsonCacheMethod(String url, Method method, JsonServerCacheLevel level) {
        this.url = url;
        this.method = method;
        this.dynamic = true;
        cacheLevel = (level == JsonServerCacheLevel.DISK_CACHE) ? JsonLocalCacheLevel.DISK_CACHE : JsonLocalCacheLevel.DISK_DATA;
    }

    public JsonCacheMethod(String url, Method method, String hash, Long time, JsonServerCacheLevel level) {
        this.url = url;
        this.method = method;
        this.hash = hash;
        this.time = time;
        this.dynamic = true;
        cacheLevel = (level == JsonServerCacheLevel.DISK_CACHE) ? JsonLocalCacheLevel.DISK_CACHE : JsonLocalCacheLevel.DISK_DATA;
    }

    public JsonCacheMethod(String test, int testRevision, String url, Method method, JsonLocalCacheLevel level) {
        this.test = test;
        this.testRevision = testRevision;
        this.url = url;
        this.method = method;
        this.cacheLevel = level;
    }

    public JsonLocalCacheLevel getCacheLevel() {
        return cacheLevel;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public String getTest() {
        return test;
    }

    public int getTestRevision() {
        return testRevision;
    }

    public String getUrl() {
        return url;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return method.getName();
    }

    public String getHash() {
        return hash;
    }

    public Long getTime() {
        return time;
    }
}