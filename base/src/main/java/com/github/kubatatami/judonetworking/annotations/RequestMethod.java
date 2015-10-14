package com.github.kubatatami.judonetworking.annotations;

import com.github.kubatatami.judonetworking.Request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMethod {

    int id() default 0;

    String name() default "";

    String[] paramNames() default {};

    int timeout() default 0;

    boolean async() default true;

    boolean highPriority() default false;

    boolean allowEmptyResult() default false;

    Class<? extends Modifier> modifier() default DefaultModifier.class;

    final class DefaultModifier implements Modifier {

        @Override
        public void modify(Request request) {

        }
    }

    /**
     * Created by Kuba on 11/07/14.
     */
    interface Modifier {

        void modify(Request request);

    }
}
