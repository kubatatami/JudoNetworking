package com.jsonrpclib;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 20.08.2013
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRequiredList {
    int minSize() default -1;
    int maxSize() default -1;
}
