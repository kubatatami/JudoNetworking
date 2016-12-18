package com.github.kubatatami.judonetworking.batches;

import com.github.kubatatami.judonetworking.builders.WrapBatchBuilder;
import com.github.kubatatami.judonetworking.callbacks.Callback;

public class WrapBatch<T, S> extends WrapBatchBuilder.LambdaBatch<T, S> {

    public WrapBatch() {
    }

    public WrapBatch(Callback<S> outerCallback) {
        super(outerCallback);
    }

    public WrapBatch(WrapBatchBuilder<T, S, ?> builder) {
        super(builder);
    }

    @Override
    public void run(T api) {
        super.run(api);
    }

    @Override
    public void runNonFatal(T api) {
        super.runNonFatal(api);
    }

    public static class Builder<T, S> extends WrapBatchBuilder<T, S, Builder<T, S>> {

        public Builder() {
        }

        public Builder(Callback<S> outerCallback) {
            super(outerCallback);
        }

    }
}
