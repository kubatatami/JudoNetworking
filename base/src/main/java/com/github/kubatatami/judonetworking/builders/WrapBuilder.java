package com.github.kubatatami.judonetworking.builders;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.builders.operators.BinaryFunction;
import com.github.kubatatami.judonetworking.builders.operators.DualOperator;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;

@SuppressWarnings("unchecked")
public abstract class WrapBuilder<T, S, Z extends WrapBuilder<T, S, ?>> extends Builder<T, Z> {

    protected DualOperator<CacheInfo, AsyncResult> onStart;

    protected BinaryFunction<T, S> onSuccess;

    protected Callback<S> outerCallback;

    public WrapBuilder() {
    }

    public WrapBuilder(Callback<S> outerCallback) {
        this.outerCallback = outerCallback;
    }

    public Z onStart(DualOperator<CacheInfo, AsyncResult> val) {
        onStart = val;
        return (Z) this;
    }

    public Z onSuccess(BinaryFunction<T, S> val) {
        onSuccess = val;
        return (Z) this;
    }

    public static abstract class LambdaCallback<T, S> extends Builder.LambdaBase<T> implements Callback<T> {

        private DualOperator<CacheInfo, AsyncResult> onStart;

        private BinaryFunction<T, S> onSuccess;

        private Callback<S> outerCallback;

        public LambdaCallback() {
        }

        public LambdaCallback(Callback<S> outerCallback) {
            this.outerCallback = outerCallback;
        }

        public LambdaCallback(WrapBuilder<T, S, ?> builder) {
            super(builder);
            this.onStart = builder.onStart;
            this.onSuccess = builder.onSuccess;
            this.outerCallback = builder.outerCallback;
        }

        protected Boolean hasCallback() {
            return outerCallback != null;
        }

        @Override
        public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
            if (onStart != null) {
                onStart.invoke(cacheInfo, asyncResult);
            }
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
            return StatefulCache.calcHashCode(
                    onStart, super.getId());
        }
    }

}
