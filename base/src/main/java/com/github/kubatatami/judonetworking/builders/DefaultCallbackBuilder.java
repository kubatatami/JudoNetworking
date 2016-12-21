package com.github.kubatatami.judonetworking.builders;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.builders.operators.BinaryOperator;
import com.github.kubatatami.judonetworking.builders.operators.DualOperator;
import com.github.kubatatami.judonetworking.builders.operators.VoidOperator;
import com.github.kubatatami.judonetworking.callbacks.DefaultCallback;
import com.github.kubatatami.judonetworking.callbacks.Identifiable;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;

@SuppressWarnings("unchecked")
public class DefaultCallbackBuilder<T, Z extends ResultBuilder<T, ?>> extends ResultBuilder<T, Z> {

    protected DualOperator<CacheInfo, AsyncResult> onStart;

    protected DualOperator<T, CacheInfo> onSuccessWithCacheInfo;

    public DefaultCallbackBuilder() {
    }

    public Z onSuccessWithCacheInfo(DualOperator<T, CacheInfo> val) {
        onSuccessWithCacheInfo = val;
        return (Z) this;
    }

    public Z onStart(DualOperator<CacheInfo, AsyncResult> val) {
        onStart = val;
        return (Z) this;
    }

    public LambdaCallback<T> build() {
        return new LambdaCallback<>(this);
    }

    public static class LambdaCallback<T> extends DefaultCallback<T> implements Identifiable {

        private BinaryOperator<T> onSuccess;

        private DualOperator<T, AsyncResult> onSuccessWithAsyncResult;

        private DualOperator<T, CacheInfo> onSuccessWithCacheInfo;

        private BinaryOperator<JudoException> onError;

        private BinaryOperator<Integer> onProgress;

        private DualOperator<CacheInfo, AsyncResult> onStart;

        private VoidOperator onFinish;

        private BinaryOperator<AsyncResult> onFinishWithAsyncResult;

        public LambdaCallback() {
        }

        public LambdaCallback(DefaultCallbackBuilder<T, ?> builder) {
            onSuccess = builder.onSuccess;
            onSuccessWithAsyncResult = builder.onSuccessWithAsyncResult;
            onError = builder.onError;
            onProgress = builder.onProgress;
            onStart = builder.onStart;
            onFinish = builder.onFinish;
            onFinishWithAsyncResult = builder.onFinishWithAsyncResult;
            onSuccessWithCacheInfo = builder.onSuccessWithCacheInfo;
        }

        @Override
        public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
            super.onStart(cacheInfo, asyncResult);
            if (onStart != null) {
                onStart.invoke(cacheInfo, asyncResult);
            }
        }

        @Override
        public void onProgress(int progress) {
            super.onProgress(progress);
            if (onProgress != null) {
                onProgress.invoke(progress);
            }
        }

        @Override
        public void onSuccess(T result) {
            super.onSuccess(result);
            if (onSuccess != null) {
                onSuccess.invoke(result);
            }
            if (onSuccessWithAsyncResult != null) {
                onSuccessWithAsyncResult.invoke(result, getAsyncResult());
            }
            if (onSuccessWithCacheInfo != null) {
                onSuccessWithCacheInfo.invoke(result, getCacheInfo());
            }
        }

        @Override
        public void onError(JudoException e) {
            super.onError(e);
            if (onError != null) {
                onError.invoke(e);
            }
        }

        @Override
        public void onFinish() {
            super.onFinish();
            if (onFinish != null) {
                onFinish.invoke();
            }
            if (onFinishWithAsyncResult != null) {
                onFinishWithAsyncResult.invoke(getAsyncResult());
            }
        }

        @Override
        public int getId() {
            return StatefulCache.calcHashCode(
                    onStart,
                    onProgress,
                    onSuccess,
                    onSuccessWithAsyncResult,
                    onSuccessWithCacheInfo,
                    onError,
                    onFinish,
                    onFinishWithAsyncResult);
        }

    }

}
