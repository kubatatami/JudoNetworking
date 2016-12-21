package com.github.kubatatami.judonetworking.builders;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.batches.DefaultBatch;
import com.github.kubatatami.judonetworking.builders.operators.BinaryOperator;
import com.github.kubatatami.judonetworking.builders.operators.DualOperator;
import com.github.kubatatami.judonetworking.builders.operators.VoidOperator;
import com.github.kubatatami.judonetworking.callbacks.Identifiable;
import com.github.kubatatami.judonetworking.callbacks.MergeCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;

@SuppressWarnings("unchecked")
public class DefaultBatchBuilder<T, Z extends ResultBuilder<Object[], ?>> extends ResultBuilder<Object[], Z> implements BatchBuilder<T> {

    protected BinaryOperator<AsyncResult> onStart;

    protected BinaryOperator<T> run;

    protected BinaryOperator<T> runNonFatal;

    protected MergeCallback<?> mergeCallback;

    public DefaultBatchBuilder() {
    }

    public DefaultBatchBuilder(MergeCallback<?> mergeCallback) {
        this.mergeCallback = mergeCallback;
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

    @Override
    public LambdaBatch<T> build() {
        return new LambdaBatch<>(this);
    }

    public static class LambdaBatch<T> extends DefaultBatch<T> implements Identifiable {

        private BinaryOperator<Object[]> onSuccess;

        private DualOperator<Object[], AsyncResult> onSuccessWithAsyncResult;

        private BinaryOperator<JudoException> onError;

        private BinaryOperator<Integer> onProgress;

        private BinaryOperator<AsyncResult> onStart;

        private VoidOperator onFinish;

        private BinaryOperator<AsyncResult> onFinishWithAsyncResult;

        private BinaryOperator<T> run;

        private BinaryOperator<T> runNonFatal;

        public LambdaBatch() {
        }

        public LambdaBatch(DefaultBatchBuilder<T, ?> builder) {
            super(builder.mergeCallback);
            onSuccess = builder.onSuccess;
            onSuccessWithAsyncResult = builder.onSuccessWithAsyncResult;
            onError = builder.onError;
            onProgress = builder.onProgress;
            onStart = builder.onStart;
            onFinish = builder.onFinish;
            onFinishWithAsyncResult = builder.onFinishWithAsyncResult;
            run = builder.run;
            runNonFatal = builder.runNonFatal;
        }

        @Override
        public void run(T api) {
            super.run(api);
            if (run != null) {
                run.invoke(api);
            }
        }

        @Override
        public void runNonFatal(T api) {
            super.runNonFatal(api);
            if (runNonFatal != null) {
                runNonFatal.invoke(api);
            }
        }

        @Override
        public void onStart(AsyncResult asyncResult) {
            super.onStart(asyncResult);
            if (onStart != null) {
                onStart.invoke(asyncResult);
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
        public void onSuccess(Object[] result) {
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

        @Override
        public int getId() {
            return StatefulCache.calcHashCode(
                    onStart,
                    onProgress,
                    onSuccess,
                    onSuccessWithAsyncResult,
                    onError,
                    onFinish,
                    onFinishWithAsyncResult);
        }

    }

}
