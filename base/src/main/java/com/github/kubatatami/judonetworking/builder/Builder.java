package com.github.kubatatami.judonetworking.builder;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.builder.operators.BinaryOperator;
import com.github.kubatatami.judonetworking.builder.operators.DualOperator;
import com.github.kubatatami.judonetworking.builder.operators.VoidOperator;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

@SuppressWarnings("unchecked")
public abstract class Builder<T, Z extends Builder<T, ?>> {

    protected BinaryOperator<T> onSuccess;

    protected DualOperator<T, AsyncResult> onSuccessWithAsyncResult;

    protected BinaryOperator<JudoException> onError;

    protected BinaryOperator<Integer> onProgress;

    protected VoidOperator onFinish;

    protected BinaryOperator<AsyncResult> onFinishWithAsyncResult;

    public Builder() {
    }

    public Z onSuccess(BinaryOperator<T> val) {
        onSuccess = val;
        return (Z) this;
    }

    public Z onSuccessWithAsyncResult(DualOperator<T, AsyncResult> val) {
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

    public Z onFinish(VoidOperator val) {
        onFinish = val;
        return (Z) this;
    }

    public Z onFinish(BinaryOperator<AsyncResult> val) {
        onFinishWithAsyncResult = val;
        return (Z) this;
    }

}
