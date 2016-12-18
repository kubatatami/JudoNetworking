package com.github.kubatatami.judonetworking.builders.operators;

public interface BinaryFunction<T, Z> {

    Z invoke(T t);
}
