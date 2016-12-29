package com.github.kubatatami.judonetworking.builders;

import com.github.kubatatami.judonetworking.callbacks.Callback;

public interface CallbackBuilder<T> {

    Callback<T> build();

}
