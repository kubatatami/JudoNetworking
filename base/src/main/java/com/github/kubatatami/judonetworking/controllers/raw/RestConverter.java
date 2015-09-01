package com.github.kubatatami.judonetworking.controllers.raw;

/**
 * Created by Kuba on 01/09/15.
 */
public abstract class RestConverter<T> {

    public abstract String convert(T value);

}
