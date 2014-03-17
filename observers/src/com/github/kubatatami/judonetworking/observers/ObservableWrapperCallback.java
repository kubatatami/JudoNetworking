package com.github.kubatatami.judonetworking.observers;

import com.github.kubatatami.judonetworking.Callback;

public class ObservableWrapperCallback<T> extends Callback<T> {

    protected final ObservableWrapper<T> wrapper;

    public ObservableWrapperCallback(ObservableWrapper<T> wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void onSuccess(T result) {
        wrapper.set(result);
    }

}
