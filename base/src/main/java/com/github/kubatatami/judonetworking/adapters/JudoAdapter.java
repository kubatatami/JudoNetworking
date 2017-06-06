package com.github.kubatatami.judonetworking.adapters;

import com.github.kubatatami.judonetworking.internals.MethodInfo;

import java.lang.reflect.Type;

public interface JudoAdapter {
    boolean canHandle(Type type);
    MethodInfo getMethodInfo(Type returnType, Object[] args, Type[] types);
}
