package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

public class LambdaCallback<T> extends DefaultCallback<T> {

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

    public LambdaCallback(BinaryOperator<T> onSuccess, BinaryOperator<JudoException> onError, BinaryOperator<Integer> onProgress, VoidOperator onFinish,
                          DualOperator<CacheInfo, AsyncResult> onStart) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onProgress = onProgress;
        this.onStart = onStart;
        this.onFinish = onFinish;
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
    }

    public interface VoidOperator {

        void invoke();
    }

    public interface BinaryOperator<T> {

        void invoke(T first);
    }

    public interface DualOperator<T, Z> {

        void invoke(T first, Z second);
    }
}
