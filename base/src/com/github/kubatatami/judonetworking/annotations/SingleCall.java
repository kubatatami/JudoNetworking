package com.github.kubatatami.judonetworking.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Kuba on 21/02/14.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SingleCall {
    boolean enabled() default true;
    SingleMode mode() default SingleMode.CANCEL_NEW;

    /**
     * Created by Kuba on 08/05/14.
     */
    public enum SingleMode {
        CANCEL_OLD, CANCEL_NEW
    }
}