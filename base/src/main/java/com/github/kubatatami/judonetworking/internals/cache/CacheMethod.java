package com.github.kubatatami.judonetworking.internals.cache;

import com.github.kubatatami.judonetworking.annotations.LocalCache;
import com.github.kubatatami.judonetworking.annotations.RequestMethod;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;

import java.lang.reflect.Method;

public class CacheMethod {

    private String url;


    private String interfaceName;

    private int methodId;

    private String methodName;

    private Long time;

    private LocalCache.CacheLevel cacheLevel;

    public CacheMethod(int methodId, String methodName, String interfaceName, String url, LocalCache.CacheLevel level) {
        this.methodId = methodId;
        this.methodName = methodName;
        this.interfaceName = interfaceName;
        this.url = url;
        this.time = System.currentTimeMillis();
        this.cacheLevel = level;

    }

    public LocalCache.CacheLevel getCacheLevel() {
        return cacheLevel;
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