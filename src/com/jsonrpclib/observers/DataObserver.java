package com.jsonrpclib.observers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.02.2013
 * Time: 16:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataObserver {
    String fieldName() default "";
    boolean onStartup() default true;
    Class<?> observerClass() default Void.class;
}
