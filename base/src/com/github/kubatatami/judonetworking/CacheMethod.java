package com.github.kubatatami.judonetworking;

import java.lang.reflect.Method;

public class CacheMethod {
    private String test;
    private int testRevision;
    private String url;
    private Method method;
    private boolean dynamic = false;
    private String hash;
    private Long time;
    private LocalCacheLevel cacheLevel;

    public CacheMethod(String url, Method method, ServerCacheLevel level) {
        this.url = url;
        this.method = method;
        this.dynamic = true;
        cacheLevel = (level == ServerCacheLevel.DISK_CACHE) ? LocalCacheLevel.DISK_CACHE : LocalCacheLevel.DISK_DATA;
    }

    public CacheMethod(String url, Method method, String hash, Long time, ServerCacheLevel level) {
        this.url = url;
        this.method = method;
        this.hash = hash;
        this.time = time;
        this.dynamic = true;
        cacheLevel = (level == ServerCacheLevel.DISK_CACHE) ? LocalCacheLevel.DISK_CACHE : LocalCacheLevel.DISK_DATA;
    }

    public CacheMethod(String test, int testRevision, String url, Method method, LocalCacheLevel level) {
        this.test = test;
        this.testRevision = testRevision;
        this.url = url;
        this.method = method;
        this.cacheLevel = level;
    }

    public LocalCacheLevel getCacheLevel() {
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