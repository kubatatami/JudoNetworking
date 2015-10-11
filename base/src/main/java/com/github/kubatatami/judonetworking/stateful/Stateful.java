package com.github.kubatatami.judonetworking.stateful;

interface Stateful<T> {

    void setCallback(T callback);

    void tryCancel();
}