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
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface LocalCache {

    public boolean enabled() default true;

    public CacheLevel cacheLevel() default CacheLevel.MEMORY_ONLY;

    public int lifeTime() default 0;

    public int size() default 0;

    public OnlyOnError onlyOnError() default OnlyOnError.NO;


    /**
     * Created with IntelliJ IDEA.
     * User: jbogacki
     * Date: 15.10.2013
     * Time: 09:05
     * To change this template use File | Settings | File Templates.
     */
    public enum CacheLevel {
        MEMORY_ONLY, DISK_CACHE, DISK_DATA
    }

    /**
     * Created by Kuba on 16/06/14.
     */
    public enum OnlyOnError {
        NO, ON_CONNECTION_ERROR, ON_ALL_ERROR
    }
}
