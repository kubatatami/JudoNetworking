package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

public class LambdaCallback<T> extends DefaultCallback<T> {

    private OnSuccess<T> onSuccess;

    private OnSuccessWithAsyncResult<T> onSuccessWithAsyncResult;

    private OnError onError;

    private OnProgress onProgress;

    private OnStart onStart;

    private OnFinish onFinish;

    private OnFinishWithAsyncResult onFinishWithAsyncResult;

    public LambdaCallback(OnSuccess<T> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public LambdaCallback(OnSuccessWithAsyncResult<T> onSuccess) {
        this.onSuccessWithAsyncResult = onSuccess;
    }

    public LambdaCallback(OnSuccess<T> onSuccess, OnError onError) {
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    public LambdaCallback(OnSuccessWithAsyncResult<T> onSuccess, OnError onError) {
        this.onSuccessWithAsyncResult = onSuccess;
        this.onError = onError;
    }

    public LambdaCallback(OnSuccess<T> onSuccess, OnError onError, OnProgress onProgress) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
    }

    public LambdaCallback(OnSuccessWithAsyncResult<T> onSuccess, OnError onError, OnProgress onProgress) {
        this.onSuccessWithAsyncResult = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
    }

    public LambdaCallback(OnSuccess<T> onSuccess, OnError onError, OnProgress onProgress, OnFinish onFinish,
                          OnStart onStart) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onStart = onStart;
        this.onFinish = onFinish;
    }

    public LambdaCallback(OnSuccessWithAsyncResult<T> onSuccess, OnError onError, OnProgress onProgress, OnFinish onFinish,
                          OnStart onStart) {
        this.onSuccessWithAsyncResult = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onStart = onStart;
        this.onFinish = onFinish;
    }

    public LambdaCallback(OnSuccess<T> onSuccess, OnError onError, OnProgress onProgress,
                          OnFinishWithAsyncResult onFinish) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onFinishWithAsyncResult = onFinish;
    }

    public LambdaCallback(OnSuccessWithAsyncResult<T> onSuccess, OnError onError, OnProgress onProgress,
                          OnFinishWithAsyncResult onFinish) {
        this.onSuccessWithAsyncResult = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onFinishWithAsyncResult = onFinish;
    }

    public LambdaCallback(OnSuccess<T> onSuccess, OnError onError, OnProgress onProgress,
                          OnFinishWithAsyncResult onFinish,
                          OnStart onStart) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onStart = onStart;
        this.onFinishWithAsyncResult = onFinish;
    }

    public LambdaCallback(OnSuccessWithAsyncResult<T> onSuccess, OnError onError, OnProgress onProgress,
                          OnFinishWithAsyncResult onFinish,
                          OnStart onStart) {
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

    public interface OnSuccess<T> extends BinaryOperator<T> {

    }

    public interface OnSuccessWithAsyncResult<T> extends DualOperator<T, AsyncResult> {

    }

    public interface OnError extends BinaryOperator<JudoException> {

    }

    public interface OnProgress extends BinaryOperator<Integer> {

    }

    public interface OnStart extends DualOperator<CacheInfo, AsyncResult> {

    }

    public interface OnFinish extends VoidOperator {

    }

    public interface OnFinishWithAsyncResult extends BinaryOperator<AsyncResult> {

    }

}
