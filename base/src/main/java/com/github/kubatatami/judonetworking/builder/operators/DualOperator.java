package com.github.kubatatami.judonetworking.builder.operators;

public interface DualOperator<T, Z> {

    void invoke(T t, Z u);
}
