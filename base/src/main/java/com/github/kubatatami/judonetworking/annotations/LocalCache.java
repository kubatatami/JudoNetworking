package com.github.kubatatami.judonetworking.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 24.09.2013
 * Time: 19:20
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LocalCache {

    boolean enabled() default true;

    CacheLevel cacheLevel() default CacheLevel.MEMORY_ONLY;

    int lifeTime() default DEFAULT;

    int size() default DEFAULT;

    OnlyOnError onlyOnError() default OnlyOnError.NO;


    int DEFAULT = -1;

    int INFINITE = 0;

    enum CacheLevel {
        MEMORY_ONLY, DISK_CACHE, DISK_DATA
    }

    enum OnlyOnError {
        NO, ON_CONNECTION_ERROR, ON_ALL_ERROR
    }
}
