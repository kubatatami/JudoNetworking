package com.github.kubatatami.judonetworking.internals.cache;

import com.github.kubatatami.judonetworking.annotations.LocalCache;
import com.github.kubatatami.judonetworking.annotations.RequestMethod;
import com.github.kubatatami.judonetworking.annotations.ServerCache;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;

import java.lang.reflect.Method;

public class CacheMethod {

    private String test;

    private int testRevision;

    private String url;

    private boolean dynamic = false;

    private String hash;

    private String interfaceName;

    private int methodId;

    private String methodName;

    private Long time;

    private LocalCache.CacheLevel cacheLevel;

    public CacheMethod(int methodId, String methodName, String interfaceName, String url, ServerCache.CacheLevel level) {
        this.methodId = methodId;
        this.methodName = methodName;
        this.interfaceName = interfaceName;
        this.url = url;
        this.dynamic = true;
        this.time = System.currentTimeMillis();
        cacheLevel = (level == ServerCache.CacheLevel.DISK_CACHE) ? LocalCache.CacheLevel.DISK_CACHE : LocalCache.CacheLevel.DISK_DATA;
    }

    public CacheMethod(int methodId, String methodName, String interfaceName, String url, String hash, Long time, ServerCache.CacheLevel level) {
        this.methodId = methodId;
        this.methodName = methodName;
        this.interfaceName = interfaceName;
        this.url = url;
        this.hash = hash;
        this.time = time;
        this.dynamic = true;
        cacheLevel = (level == ServerCache.CacheLevel.DISK_CACHE) ? LocalCache.CacheLevel.DISK_CACHE : LocalCache.CacheLevel.DISK_DATA;
    }

    public CacheMethod(int methodId, String methodName, String interfaceName, String test, int testRevision, String url, LocalCache.CacheLevel level) {
        this.methodId = methodId;
        this.methodName = methodName;
        this.interfaceName = interfaceName;
        this.test = test;
        this.testRevision = testRevision;
        this.url = url;
        this.time = System.currentTimeMillis();
        this.cacheLevel = level;
        if (test == null) {
            this.dynamic = true;
        }
    }

    public LocalCache.CacheLevel getCacheLevel() {
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
        return methodId;
    }

    @Override
    public String toString() {
        return methodName;
    }

    public String getHash() {
        return hash;
    }

    public Long getTime() {
        return time;
    }


    public String getInterfaceName() {
        return interfaceName;
    }

    public static int getMethodId(Method method) {
        RequestMethod requestMethod = ReflectionCache.getAnnotation(method, RequestMethod.class);
        return requestMethod.id() == 0 ? method.hashCode() : requestMethod.id();
    }
}