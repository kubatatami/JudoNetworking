package com.github.kubatatami.judonetworking.builders;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.builders.operators.BinaryOperator;
import com.github.kubatatami.judonetworking.builders.operators.VoidOperator;
import com.github.kubatatami.judonetworking.callbacks.BaseCallback;
import com.github.kubatatami.judonetworking.callbacks.Identifiable;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;

@SuppressWarnings("unchecked")
public abstract class Builder<T, Z extends Builder<T, ?>> {

    protected BinaryOperator<JudoException> onError;

    protected BinaryOperator<Integer> onProgress;

    protected VoidOperator onFinish;

    protected BinaryOperator<AsyncResult> onFinishWithAsyncResult;

    public Builder() {
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

    public static abstract class LambdaBase<T> implements BaseCallback<T>, Identifiable {

        private BinaryOperator<JudoException> onError;

        private BinaryOperator<Integer> onProgress;

        private VoidOperator onFinish;

        public LambdaBase() {
        }

        public LambdaBase(Builder<T, ?> builder) {
            onError = builder.onError;
            onProgress = builder.onProgress;
            onFinish = builder.onFinish;
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
                    onProgress,
                    onError,
                    onFinish);
        }

    }

}
