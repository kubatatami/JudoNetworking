package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.builders.WrapBuilder;
import com.github.kubatatami.judonetworking.builders.operators.BinaryFunction;

public class CastCallback<T extends S, S> extends WrapBuilder.LambdaCallback<T, S> {

    private BinaryFunction<T, S> onSuccess;

    private Callback<S> outerCallback;

    public CastCallback() {
    }

    public CastCallback(Callback<S> outerCallback) {
        super(outerCallback);
        this.outerCallback = outerCallback;
    }

    public CastCallback(Builder<T, S> builder) {
        super(builder);
        this.outerCallback = builder.outerCallback;
        this.onSuccess = builder.onSuccess;
    }

    @Override
    public void onSuccess(T result) {
        S newResult = result;
        if (onSuccess != null) {
            newResult = onSuccess.invoke(result);
        }
        if (hasCallback()) {
            outerCallback.onSuccess(newResult);
        }
    }

    public static class Builder<T extends S, S> extends WrapBuilder<T, S, Builder<T, S>> {

        private BinaryFunction<T, S> onSuccess;

        private Callback<S> outerCallback;;

        public Builder() {
        }

        public Builder(Callback<S> outerCallback) {
            super(outerCallback);
            this.outerCallback = outerCallback;
        }


        @Override
        public Builder<T, S> onSuccess(BinaryFunction<T, S> val) {
            this.onSuccess = val;
            return super.onSuccess(val);
        }

        @Override
        public CastCallback<T, S> build() {
            return new CastCallback<>(this);
        }

    }
}
