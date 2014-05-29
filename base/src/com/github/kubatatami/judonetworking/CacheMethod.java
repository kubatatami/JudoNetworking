package com.github.kubatatami.judonetworking;

import java.lang.reflect.Method;

public class CacheMethod {
    private String test;
    private int testRevision;
    private String url;
    private boolean dynamic = false;
    private String hash;
    private Long time;
    private LocalCacheLevel cacheLevel;
    private RequestInterface request;

    public CacheMethod(RequestInterface request, String url, ServerCacheLevel level) {
        this.request = request;
        this.url = url;
        this.dynamic = true;
        this.time = System.currentTimeMillis();
        cacheLevel = (level == ServerCacheLevel.DISK_CACHE) ? LocalCacheLevel.DISK_CACHE : LocalCacheLevel.DISK_DATA;
    }

    public CacheMethod(RequestInterface request, String url, String hash, Long time, ServerCacheLevel level) {
        this.request = request;
        this.url = url;
        this.hash = hash;
        this.time = time;
        this.dynamic = true;
        cacheLevel = (level == ServerCacheLevel.DISK_CACHE) ? LocalCacheLevel.DISK_CACHE : LocalCacheLevel.DISK_DATA;
    }

    public CacheMethod(RequestInterface request, String test, int testRevision, String url, LocalCacheLevel level) {
        this.request = request;
        this.test = test;
        this.testRevision = testRevision;
        this.url = url;
        this.time = System.currentTimeMillis();
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

    public int getMethodId() {
        return request.getMethodId();
    }

    @Override
    public String toString() {
        return request.getMethod().getName();
    }

    public String getHash() {
        return hash;
    }

    public Long getTime() {
        return time;
    }


    public Method getMethod() {
        return request.getMethod();
    }

    public static int getMethodId(Method method) {
        RequestMethod requestMethod = ReflectionCache.getAnnotation(method,RequestMethod.class);
        return requestMethod.id()==0 ? method.hashCode() : requestMethod.id();
    }
}