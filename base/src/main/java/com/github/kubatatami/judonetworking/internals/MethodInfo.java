package com.github.kubatatami.judonetworking.internals;

import com.github.kubatatami.judonetworking.callbacks.Callback;

import java.lang.reflect.Type;

public class MethodInfo {

    private final Callback<Object> callback;
    private final Type resultType;
    private final Object[] args;

    public MethodInfo(Callback<Object> callback, Type resultType, Object[] args) {
        this.callback = callback;
        this.resultType = resultType;
        this.args = args;
    }

    public Callback<Object> getCallback() {
        return callback;
    }

    public Type getResultType() {
        return resultType;
    }

    public Object[] getArgs() {
        return args;
    }
}
