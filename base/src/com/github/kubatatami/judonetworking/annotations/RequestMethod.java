package com.github.kubatatami.judonetworking.annotations;

import com.github.kubatatami.judonetworking.internals.RequestInterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMethod {

    public int id() default 0;

    public String name() default "";

    public String[] paramNames() default {};

    public int timeout() default 0;

    public boolean async() default false;

    public boolean highPriority() default false;

    public boolean allowEmptyResult() default false;

    public Class<? extends Modifier> modifier() default DefaultModifier.class;

    static final class DefaultModifier implements Modifier {
        @Override
        public void modify(RequestInterface request) {

        }
    }

    /**
     * Created by Kuba on 11/07/14.
     */
    public interface Modifier {

        public void modify(RequestInterface request);

    }
}
