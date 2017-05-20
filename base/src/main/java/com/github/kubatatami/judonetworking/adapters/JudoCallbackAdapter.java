package com.github.kubatatami.judonetworking.adapters;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.builders.CallbackBuilder;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.internals.MethodInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class JudoCallbackAdapter implements JudoAdapter {

    @Override
    public List<Class<?>> getReturnClass() {
        return Arrays.asList(AsyncResult.class, Void.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MethodInfo getMethodInfo(Object[] args, Type[] types) {
        Callback<Object> callback = null;
        Type resultType = Void.class;
        if (args.length > 0 && args[args.length - 1] instanceof Callback) {
            callback = (Callback<Object>) args[args.length - 1];
            resultType = ((ParameterizedType) types[args.length - 1]).getActualTypeArguments()[0];
        } else if (args.length > 0 && args[args.length - 1] instanceof CallbackBuilder) {
            callback = ((CallbackBuilder<Object>) args[args.length - 1]).build();
            resultType = ((ParameterizedType) types[args.length - 1]).getActualTypeArguments()[0];
        } else if (types.length > 0 && types[types.length - 1] instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) types[types.length - 1];
            if (parameterizedType.getRawType().equals(Callback.class)) {
                resultType = parameterizedType.getActualTypeArguments()[0];
            }
        }

        Object[] newArgs;
        if (args.length > 1) {
            newArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, newArgs, 0, args.length - 1);
        } else {
            newArgs = null;
        }

        return new MethodInfo(callback, resultType, newArgs);
    }
}
