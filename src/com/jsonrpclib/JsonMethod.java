package com.jsonrpclib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonMethod {
    public String name() default "";

    public String[] paramNames() default {};

    public int timeout() default 0;

    public boolean async() default false;

    public boolean cacheable() default false;

    public boolean cachePersist() default false;

    public int cacheLifeTime() default 0;

    public int cacheSize() default 100;

    public boolean highPriority() default false;
}
