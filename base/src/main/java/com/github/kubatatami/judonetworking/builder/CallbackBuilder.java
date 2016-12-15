package com.github.kubatatami.judonetworking.builder;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.callbacks.DefaultCallback;
import com.github.kubatatami.judonetworking.callbacks.Identifiable;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;

@SuppressWarnings("unchecked")
public class CallbackBuilder<T, Z extends CallbackBuilder<T, ?>> {

    BinaryOperator<T> onSuccess;

    DualOperator<T, AsyncResult> onSuccessWithAsyncResult;

    BinaryOperator<JudoException> onError;

    BinaryOperator<Integer> onProgress;

    DualOperator<CacheInfo, AsyncResult> onStart;

    VoidOperator onFinish;

    BinaryOperator<AsyncResult> onFinishWithAsyncResult;

    public CallbackBuilder() {
    }

    public Z onSuccess(BinaryOperator<T> val) {
        onSuccess = val;
        return (Z) this;
    }

    public Z onSuccess(DualOperator<T, AsyncResult> val) {
        onSuccessWithAsyncResult = val;
        return (Z) this;
    }

    public Z onError(BinaryOperator<JudoException> val) {
        onError = val;
        return (Z) this;
    }

    public Z onProgress(BinaryOperator<Integer> val) {
        onProgress = val;
        return (Z) this;
    }

    public Z onStart(DualOperator<CacheInfo, AsyncResult> val) {
        onStart = val;
        return (Z) this;
    }

    public Z onFinish(VoidOperator val) {
        onFinish = val;
        return (Z) this;
    }

    public Z onFinish(BinaryOperator<AsyncResult> val) {
        onFinishWithAsyncResult = val;
        return (Z) this;
    }

    public LambdaCallback<T> build() {
        return new LambdaCallback<>(this);
    }

    public static class LambdaCallback<T> extends DefaultCallback<T> implements Identifiable {

        private BinaryOperator<T> onSuccess;

        private DualOperator<T, AsyncResult> onSuccessWithAsyncResult;

        private BinaryOperator<JudoException> onError;

        private BinaryOperator<Integer> onProgress;

        private DualOperator<CacheInfo, AsyncResult> onStart;

        private VoidOperator onFinish;

        private BinaryOperator<AsyncResult> onFinishWithAsyncResult;

        public LambdaCallback(CallbackBuilder<T, ?> builder) {
            onSuccess = builder.onSuccess;
            onSuccessWithAsyncResult = builder.onSuccessWithAsyncResult;
            onError = builder.onError;
            onProgress = builder.onProgress;
            onStart = builder.onStart;
            onFinish = builder.onFinish;
            onFinishWithAsyncResult = builder.onFinishWithAsyncResult;
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
                    onError,
                    onFinish,
                    onFinishWithAsyncResult);
        }

    }

    public interface VoidOperator<T> {

        void invoke();
    }

    public interface BinaryOperator<T> {

        void invoke(T t);
    }

    public interface DualOperator<T, Z> {

        void invoke(T t, Z u);
    }
}
