package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

public class LambdaCallback<T> extends DefaultCallback<T> {

    private BinaryOperator<T> onSuccess;

    private DualOperator<T, AsyncResult> onSuccessWithAsyncResult;

    private BinaryOperator<JudoException> onError;

    private BinaryOperator<Integer> onProgress;

    private DualOperator<CacheInfo, AsyncResult> onStart;

    private VoidOperator onFinish;

    private BinaryOperator<AsyncResult> onFinishWithAsyncResult;

    private LambdaCallback(Builder<T> builder) {
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

    public interface VoidOperator<T> {

        void invoke();
    }

    public interface BinaryOperator<T> {

        void invoke(T t);
    }

    public interface DualOperator<T, Z> {

        void invoke(T t, Z u);
    }


    public static final class Builder<T> {

        private BinaryOperator<T> onSuccess;

        private DualOperator<T, AsyncResult> onSuccessWithAsyncResult;

        private BinaryOperator<JudoException> onError;

        private BinaryOperator<Integer> onProgress;

        private DualOperator<CacheInfo, AsyncResult> onStart;

        private VoidOperator onFinish;

        private BinaryOperator<AsyncResult> onFinishWithAsyncResult;

        public Builder() {
        }

        public Builder<T> onSuccess(BinaryOperator<T> val) {
            onSuccess = val;
            return this;
        }

        public Builder<T> onSuccess(DualOperator<T, AsyncResult> val) {
            onSuccessWithAsyncResult = val;
            return this;
        }

        public Builder<T> onError(BinaryOperator<JudoException> val) {
            onError = val;
            return this;
        }

        public Builder<T> onProgress(BinaryOperator<Integer> val) {
            onProgress = val;
            return this;
        }

        public Builder<T> onStart(DualOperator<CacheInfo, AsyncResult> val) {
            onStart = val;
            return this;
        }

        public Builder<T> onFinish(VoidOperator val) {
            onFinish = val;
            return this;
        }

        public Builder<T> onFinish(BinaryOperator<AsyncResult> val) {
            onFinishWithAsyncResult = val;
            return this;
        }

        public LambdaCallback<T> build() {
            return new LambdaCallback<>(this);
        }
    }
}
