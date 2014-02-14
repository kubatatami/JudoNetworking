package com.github.kubatatami.judonetworking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMethod {
    public String name() default "";

    public String[] paramNames() default {};

    public int timeout() default 0;

    public boolean async() default false;

    public boolean highPriority() default false;

    public boolean allowEmptyResult() default false;
}
