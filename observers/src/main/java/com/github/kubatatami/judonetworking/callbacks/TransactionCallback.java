package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.observers.ObservableTransaction;
import com.github.kubatatami.judonetworking.observers.ObservableWrapper;

public class TransactionCallback<T> extends DecoratorCallback<T> {

    private ObservableWrapper<T> wrapper;

    private ObservableTransaction transaction;

    public TransactionCallback(Builder<T> builder) {
        super(builder);
    }

    public TransactionCallback(ObservableWrapper<T> wrapper, ObservableTransaction transaction) {
        this(wrapper, transaction, null);
    }

    public TransactionCallback(ObservableWrapper<T> wrapper, ObservableTransaction transaction, Callback<T> callback) {
        super(callback);
        this.wrapper = wrapper;
        this.transaction = transaction;
    }

    @Override
    public void onSuccess(T result) {
        wrapper.set(result, transaction);
    }

    public static class Builder<T> extends DecoratorCallback.Builder<T> {

        private ObservableWrapper<T> wrapper;

        private ObservableTransaction transaction;

        public Builder(MergeCallback callback, ObservableWrapper<T> wrapper, ObservableTransaction transaction) {
            super(callback);
            this.wrapper = wrapper;
            this.transaction = transaction;
        }

        public Builder(Callback<T> callback, ObservableWrapper<T> wrapper, ObservableTransaction transaction) {
            super(callback);
            this.wrapper = wrapper;
            this.transaction = transaction;
        }

        public Builder(ObservableWrapper<T> wrapper, ObservableTransaction transaction) {
            this.wrapper = wrapper;
            this.transaction = transaction;
        }

        @Override
        public DecoratorCallback<T> build() {
            return new TransactionCallback<>(this);
        }
    }

}
