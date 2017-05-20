package com.github.kubatatami.judonetworking.adapters;

import com.github.kubatatami.judonetworking.internals.MethodInfo;

import java.lang.reflect.Type;
import java.util.List;

public interface JudoAdapter {
    List<Class<?>> getReturnClass();
    MethodInfo getMethodInfo(Object[] args, Type[] types);
}
