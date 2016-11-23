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

    public LambdaCallback(BinaryOperator<T> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public LambdaCallback(DualOperator<T, AsyncResult> onSuccess) {
        this.onSuccessWithAsyncResult = onSuccess;
    }

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError) {
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    public LambdaCallback(DualOperator<T, AsyncResult> onSuccess, BinaryOperator<JudoException> onError) {
        this.onSuccessWithAsyncResult = onSuccess;
        this.onError = onError;
    }

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
    }

    public LambdaCallback(DualOperator<T, AsyncResult> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress) {
        this.onSuccessWithAsyncResult = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
    }

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress, VoidOperator onFinish,
                          DualOperator<CacheInfo, AsyncResult> onStart) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onStart = onStart;
        this.onFinish = onFinish;
    }

    public LambdaCallback(DualOperator<T, AsyncResult> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress, VoidOperator onFinish,
                          DualOperator<CacheInfo, AsyncResult> onStart) {
        this.onSuccessWithAsyncResult = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onStart = onStart;
        this.onFinish = onFinish;
    }

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress,
                          BinaryOperator<AsyncResult> onFinish) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onFinishWithAsyncResult = onFinish;
    }

    public LambdaCallback(DualOperator<T, AsyncResult> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress,
                          BinaryOperator<AsyncResult> onFinish) {
        this.onSuccessWithAsyncResult = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onFinishWithAsyncResult = onFinish;
    }

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress,
                          BinaryOperator<AsyncResult> onFinish,
                          DualOperator<CacheInfo, AsyncResult> onStart) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onStart = onStart;
        this.onFinishWithAsyncResult = onFinish;
    }

    public LambdaCallback(DualOperator<T, AsyncResult> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress,
                          BinaryOperator<AsyncResult> onFinish,
                          DualOperator<CacheInfo, AsyncResult> onStart) {
        this.onSuccessWithAsyncResult = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onStart = onStart;
        this.onFinishWithAsyncResult = onFinish;
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
}
