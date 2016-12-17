package com.github.kubatatami.judonetworking.builder.operators;

public interface BinaryFunction<T, Z> {

    Z invoke(T t);
}
