package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

public class LambdaCallback<T> implements Callback<T> {

    BinaryOperator<T> onSuccess;

    BinaryOperator<JudoException> onError;

    BinaryOperator<Integer> onProgress;

    DualOperator<CacheInfo, AsyncResult> onStart;

    VoidOperator onFinish;

    public LambdaCallback(BinaryOperator<T> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError) {
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
    }

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress, VoidOperator onFinish) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onFinish = onFinish;
    }

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress,
                          DualOperator<CacheInfo, AsyncResult> onStart, VoidOperator onFinish) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onStart = onStart;
        this.onFinish = onFinish;
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
    public void onSuccess(T result) {
        if (onSuccess != null) {
            onSuccess.invoke(result);
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

    interface VoidOperator {

        void invoke();
    }

    interface BinaryOperator<T> {

        void invoke(T first);
    }

    interface DualOperator<T, Z> {

        void invoke(T first, Z second);
    }
}
