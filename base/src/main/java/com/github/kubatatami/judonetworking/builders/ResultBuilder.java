package com.github.kubatatami.judonetworking.builders;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.builders.operators.BinaryOperator;
import com.github.kubatatami.judonetworking.builders.operators.DualOperator;

@SuppressWarnings("unchecked")
public abstract class ResultBuilder<T, Z extends Builder<T, ?>> extends Builder<T, Z> {

    protected BinaryOperator<T> onSuccess;

    protected DualOperator<T, AsyncResult> onSuccessWithAsyncResult;

    public ResultBuilder() {
    }

    public Z onSuccess(BinaryOperator<T> val) {
        onSuccess = val;
        return (Z) this;
    }

    public Z onSuccessWithAsyncResult(DualOperator<T, AsyncResult> val) {
        onSuccessWithAsyncResult = val;
        return (Z) this;
    }

}
