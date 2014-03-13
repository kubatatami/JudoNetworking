package com.github.kubatatami.judonetworking.observers;

import com.github.kubatatami.judonetworking.Callback;

public class WrapperCallback<T> extends Callback<T> {

    protected final ObservableWrapper<T> wrapper;

    public WrapperCallback(ObservableWrapper<T> wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void onFinish(T result) {
        wrapper.set(result);
    }

}
