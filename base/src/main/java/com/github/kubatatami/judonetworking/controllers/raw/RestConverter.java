package com.github.kubatatami.judonetworking.controllers.raw;

/**
 * Created by Kuba on 01/09/15.
 */
public abstract class RestConverter<T> {

    protected Class<T> type;

    public RestConverter(Class<T> type) {
        this.type = type;
    }

    public abstract String convert(T value);

    public Class<T> getType() {
        return type;
    }
}
