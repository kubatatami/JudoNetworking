package com.github.kubatatami.judonetworking.controllers.raw;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Kuba on 01/09/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Converter {

    Class<? extends RestConverter<?>> value() default DummyConverter.class;

    class DummyConverter extends RestConverter<Object>{

        @Override
        public String convert(Object value) {
            return value + "";
        }
    }
}
