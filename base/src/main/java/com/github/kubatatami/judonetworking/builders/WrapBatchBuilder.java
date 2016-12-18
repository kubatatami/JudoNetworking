package com.github.kubatatami.judonetworking.builders;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.builders.operators.BinaryFunction;
import com.github.kubatatami.judonetworking.builders.operators.BinaryOperator;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;

@SuppressWarnings("unchecked")
public abstract class WrapBatchBuilder<T, S, Z extends WrapBatchBuilder<T, S, ?>> extends Builder<Object[], Z> {

    protected BinaryOperator<AsyncResult> onStart;

    protected BinaryFunction<Object[], S> onSuccess;

    protected BinaryOperator<T> run;

    protected BinaryOperator<T> runNonFatal;

    protected Callback<S> outerCallback;

    public WrapBatchBuilder() {
    }

    public WrapBatchBuilder(Callback<S> outerCallback) {
        this.outerCallback = outerCallback;
    }

    public Z onStart(BinaryOperator<AsyncResult> val) {
        onStart = val;
        return (Z) this;
    }

    public Z run(BinaryOperator<T> val) {
        run = val;
        return (Z) this;
    }

    public Z runNonFatal(BinaryOperator<T> val) {
        runNonFatal = val;
        return (Z) this;
    }

    public Z onSuccess(BinaryFunction<Object[], S> val) {
        onSuccess = val;
        return (Z) this;
    }

    public static abstract class LambdaBatch<T, S> extends Builder.LambdaBase<Object[]> implements Batch<T> {

        private BinaryOperator<AsyncResult> onStart;

        private BinaryFunction<Object[], S> onSuccess;

        private Callback<S> outerCallback;

        private BinaryOperator<T> run;

        private BinaryOperator<T> runNonFatal;

        public LambdaBatch() {
        }

        public LambdaBatch(Callback<S> outerCallback) {
            this.outerCallback = outerCallback;
        }

        public LambdaBatch(WrapBatchBuilder<T, S, ?> builder) {
            super(builder);
            this.onStart = builder.onStart;
            this.onSuccess = builder.onSuccess;
            this.outerCallback = builder.outerCallback;
            run = builder.run;
            runNonFatal = builder.runNonFatal;
        }

        protected Boolean hasCallback() {
            return outerCallback != null;
        }

        @Override
        public void onStart(AsyncResult asyncResult) {
            if (onStart != null) {
                onStart.invoke(asyncResult);
            }
            if (hasCallback()) {
                outerCallback.onStart(new CacheInfo(false, 0L), asyncResult);
            }
        }

        @Override
        public void run(T api) {
            if (run != null) {
                run.invoke(api);
            }
        }

        @Override
        public void runNonFatal(T api) {
            if (runNonFatal != null) {
                runNonFatal.invoke(api);
            }
        }

        @Override
        public void onSuccess(Object[] result) {
            if (hasCallback() && onSuccess != null) {
                outerCallback.onSuccess(onSuccess.invoke(result));
            }
        }

        @Override
        public void onError(JudoException e) {
            super.onError(e);
            if (hasCallback()) {
                outerCallback.onError(e);
            }
        }

        @Override
        public void onFinish() {
            super.onFinish();
            if (hasCallback()) {
                outerCallback.onFinish();
            }
        }

        @Override
        public void onProgress(int progress) {
            super.onProgress(progress);
            if (hasCallback()) {
                outerCallback.onProgress(progress);
            }
        }

        @Override
        public int getId() {
            return StatefulCache.calcHashCode(
                    onStart, run, runNonFatal, onSuccess, super.getId());
        }
    }

}

