package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.builder.WrapBuilder;
import com.github.kubatatami.judonetworking.builder.operators.BinaryFunction;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;

public class WrapCallback<T, S> extends WrapBuilder.LambdaCallback<T, S> {

    private Callback<S> outerCallback;

    private BinaryFunction<T, S> onSuccess;

    public WrapCallback(Callback<S> outerCallback) {
        this.outerCallback = outerCallback;
    }

    public WrapCallback(Builder<T, S> builder) {
        super(builder);
        this.outerCallback = builder.outerCallback;
        this.onSuccess = builder.onSuccess;
    }

    protected Boolean hasCallback() {
        return outerCallback != null;
    }

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        super.onStart(cacheInfo, asyncResult);
        if (hasCallback()) {
            outerCallback.onStart(cacheInfo, asyncResult);
        }
    }

    @Override
    public void onSuccess(T result) {
        if (hasCallback() && onSuccess != null) {
            outerCallback.onSuccess(onSuccess.invoke(result));
        }
    }

    @Override
    public void onError(JudoException e) {
        super.onError(e);
        if (hasCallback()) {
            outerCallback.onError(e);
        }
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (hasCallback()) {
            outerCallback.onFinish();
        }
    }

    @Override
    public void onProgress(int progress) {
        super.onProgress(progress);
        if (hasCallback()) {
            outerCallback.onProgress(progress);
        }
    }

    @Override
    public int getId() {
        return StatefulCache.calcHashCode(super.getId(), onSuccess);
    }

    public static class Builder<T, S> extends WrapBuilder<T, S, Builder<T, S>> {

        private Callback<S> outerCallback;

        private BinaryFunction<T, S> onSuccess;

        public Builder(Callback<S> outerCallback) {
            this.outerCallback = outerCallback;
        }

        public Builder<T, S> onSuccess(BinaryFunction<T, S> val) {
            onSuccess = val;
            return this;
        }

        public WrapCallback<T, S> build() {
            return new WrapCallback<>(this);
        }

    }
}
