package com.github.kubatatami.judonetworking.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 24.09.2013
 * Time: 19:45
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ServerCache {

    boolean enabled() default true;

    boolean useOldOnError() default false;

    int size() default 0;

    CacheLevel cacheLevel() default CacheLevel.DISK_CACHE;

    /**
     * Created with IntelliJ IDEA.
     * User: jbogacki
     * Date: 15.10.2013
     * Time: 09:05
     * To change this template use File | Settings | File Templates.
     */
    enum CacheLevel {
        DISK_CACHE, DISK_DATA
    }
}
