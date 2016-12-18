package com.github.kubatatami.judonetworking.batches;

import com.github.kubatatami.judonetworking.builders.WrapBatchBuilder;
import com.github.kubatatami.judonetworking.callbacks.Callback;

public class WrapBatch<T, S> extends WrapBatchBuilder.LambdaBatch<T, S> {

    public WrapBatch() {
    }

    public WrapBatch(Callback<S> outerCallback) {
        super(outerCallback);
    }

    public WrapBatch(WrapBatchBuilder<S, ?> builder) {
        super(builder);
    }

    @Override
    public void run(T api) {

    }

    @Override
    public void runNonFatal(T api) {

    }

    public static class Builder<T, S> extends WrapBatchBuilder<S, Builder<T, S>> {

        public Builder() {
        }

        public Builder(Callback<S> outerCallback) {
            super(outerCallback);
        }

    }
}
