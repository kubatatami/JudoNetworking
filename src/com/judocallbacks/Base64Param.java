package com.judocallbacks;

import android.util.Base64;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jbogacki on 30.01.2014.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Base64Param {
    int type() default Base64.DEFAULT;
    String prefix() default "";
    String suffix() default "";
}
