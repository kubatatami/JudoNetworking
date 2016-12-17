package com.github.kubatatami.judonetworking.builder;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.builder.operators.BinaryFunction;
import com.github.kubatatami.judonetworking.builder.operators.BinaryOperator;
import com.github.kubatatami.judonetworking.builder.operators.DualOperator;
import com.github.kubatatami.judonetworking.builder.operators.VoidOperator;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.callbacks.Identifiable;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;

@SuppressWarnings("unchecked")
public abstract class WrapBuilder<T, S, Z extends Builder<T, ?>> extends Builder<T, Z> {

    protected BinaryFunction<T, S> onSuccess;

    protected DualOperator<CacheInfo, AsyncResult> onStart;

    public WrapBuilder() {
    }

    public Z onStart(DualOperator<CacheInfo, AsyncResult> val) {
        onStart = val;
        return (Z) this;
    }

    public Z onSuccess(BinaryFunction<T, S> val) {
        onSuccess = val;
        return (Z) this;
    }

    public static abstract class LambdaCallback<T, S> implements Callback<T>, Identifiable {

        private BinaryOperator<JudoException> onError;

        private BinaryOperator<Integer> onProgress;

        private DualOperator<CacheInfo, AsyncResult> onStart;

        private VoidOperator onFinish;

        public LambdaCallback() {
        }

        public LambdaCallback(WrapBuilder<T, S, ?> builder) {
            onError = builder.onError;
            onProgress = builder.onProgress;
            onStart = builder.onStart;
            onFinish = builder.onFinish;
        }

        @Override
        public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
            if (onStart != null) {
                onStart.invoke(cacheInfo, asyncResult);
            }
        }

        @Override
        public void onProgress(int progress) {
            if (onProgress != null) {
                onProgress.invoke(progress);
            }
        }

        @Override
        public void onError(JudoException e) {
            if (onError != null) {
                onError.invoke(e);
            }
        }

        @Override
        public void onFinish() {
            if (onFinish != null) {
                onFinish.invoke();
            }
        }

        @Override
        public int getId() {
            return StatefulCache.calcHashCode(
                    onStart,
                    onProgress,
                    onError,
                    onFinish);
        }

    }
}
