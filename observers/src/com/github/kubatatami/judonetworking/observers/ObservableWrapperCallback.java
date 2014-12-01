package com.github.kubatatami.judonetworking.observers;

import com.github.kubatatami.judonetworking.Callback;

public class ObservableWrapperCallback<T> extends Callback<T> {

    protected final ObservableWrapper<T> wrapper;
    protected ObservableTransaction transaction=null;

    public ObservableWrapperCallback(ObservableWrapper<T> wrapper) {
        this.wrapper = wrapper;
    }

    public ObservableWrapperCallback(ObservableWrapper<T> wrapper, ObservableTransaction transaction) {
        this.wrapper = wrapper;
        this.transaction = transaction;
    }

    @Override
    public void onSuccess(T result) {
        if(transaction!=null){
            wrapper.set(result,transaction);
        }else {
            wrapper.set(result);
        }
    }

}
