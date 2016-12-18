package com.github.kubatatami.judonetworking.builders.operators;

public interface DualOperator<T, Z> {

    void invoke(T t, Z u);
}
