package org.judonetworking;

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
@Target(ElementType.METHOD)
public @interface LocalCache {

    public boolean enabled() default true;

    public LocalCacheLevel cacheLevel() default LocalCacheLevel.MEMORY_ONLY;

    public int lifeTime() default 0;

    public int size() default 0;

    public boolean onlyOnError() default false;
}
