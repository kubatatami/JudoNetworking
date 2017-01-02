package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.builders.operators.BinaryOperator;
import com.github.kubatatami.judonetworking.builders.operators.VoidFunction;
import com.github.kubatatami.judonetworking.builders.operators.VoidOperator;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Kuba on 28/03/15.
 */
public class MergeCallback<T> {

    private int requests;

    private int finishRequests = 0;

    private boolean started = false;

    private boolean canceled = false;

    private Map<BaseCallback<?>, Integer> progressMap = new HashMap<>();

    private Set<AsyncResult> asyncResultSet = new HashSet<>();

    private Callback<T> finalCallback;

    private BinaryOperator<JudoException> onError;

    private BinaryOperator<Integer> onProgress;

    private VoidOperator onFinish;

    private VoidOperator onStart;

    private VoidFunction<T> onSuccess;

    public MergeCallback(Builder<T> builder) {
        this.requests = builder.requests;
        this.finalCallback = builder.callback;
        this.onError = builder.onError;
        this.onProgress = builder.onProgress;
        this.onFinish = builder.onFinish;
        this.onStart = builder.onStart;
        this.onSuccess = builder.onSuccess;
    }

    public MergeCallback(int requests) {
        this.requests = requests;
    }

    public MergeCallback(int requests, Callback<T> finalCallback) {
        this.requests = requests;
        this.finalCallback = finalCallback;
    }

    public final void addStart(AsyncResult asyncResult) {
        if (canceled) {
            return;
        }
        if (!started) {
            started = true;
            onMergeStart();
        }
        asyncResultSet.add(asyncResult);
    }

    public final void addProgress(BaseCallback<?> callback, int progress) {
        if (canceled) {
            return;
        }
        progressMap.put(callback, Math.min(100, progress));
        onMergeProgress(calculateProgress());
    }

    private int calculateProgress() {
        int progress = 0;
        for (int value : progressMap.values()) {
            progress += value;
        }
        return progress / requests;
    }

    public final void addSuccess() {
        if (canceled) {
            return;
        }
        finishRequests++;
        if (finishRequests == requests) {
            onMergeSuccess();
            onMergeFinish();
        }
    }

    public final void addError(JudoException e) {
        if (canceled) {
            return;
        }
        onMergeError(e);
        onMergeFinish();
        cancelAll();
        canceled = true;
    }

    private void cancelAll() {
        for (AsyncResult asyncResult : asyncResultSet) {
            asyncResult.cancel();
        }
    }

    private void onMergeStart() {
        if (onStart != null) {
            onStart.invoke();
        }
        if (finalCallback != null) {
            finalCallback.onStart(null, new MergeAsyncResult());
        }
    }

    private void onMergeProgress(int progress) {
        if (onProgress != null) {
            onProgress.invoke(progress);
        }
        if (finalCallback != null) {
            finalCallback.onProgress(progress);
        }
    }

    private void onMergeSuccess() {
        T result = null;
        if (onSuccess != null) {
            result = onSuccess.invoke();
        }
        if (finalCallback != null) {
            finalCallback.onSuccess(result);
        }
    }

    private void onMergeError(JudoException e) {
        if (onError != null) {
            onError.invoke(e);
        }
        if (finalCallback != null) {
            finalCallback.onError(e);
        }
    }

    private void onMergeFinish() {
        if (onFinish != null) {
            onFinish.invoke();
        }
        if (finalCallback != null) {
            finalCallback.onFinish();
        }
    }

    public static <T> Builder<T> builder(int requests) {
        return new Builder<>(requests);
    }

    public static class Builder<T> {

        private int requests;

        private Callback<T> callback;

        private BinaryOperator<JudoException> onError;

        private BinaryOperator<Integer> onProgress;

        private VoidOperator onFinish;

        private VoidOperator onStart;

        private VoidFunction<T> onSuccess;

        public Builder(int requests) {
            this.requests = requests;
        }

        public Builder<T> callback(Callback<T> callback) {
            this.callback = callback;
            return this;
        }

        public Builder<T> onError(BinaryOperator<JudoException> val) {
            this.onError = val;
            return this;
        }

        public Builder<T> onProgress(BinaryOperator<Integer> val) {
            this.onProgress = val;
            return this;
        }

        public Builder<T> onSuccess(VoidFunction<T> val) {
            this.onSuccess = val;
            return this;
        }

        public Builder<T> onStart(VoidOperator val) {
            this.onStart = val;
            return this;
        }

        public Builder<T> onFinish(VoidOperator val) {
            this.onFinish = val;
            return this;
        }

        public MergeCallback<T> build() {
            return new MergeCallback<>(this);
        }
    }

    private class MergeAsyncResult implements AsyncResult {

        @Override
        public boolean isDone() {
            for (AsyncResult asyncResult : asyncResultSet) {
                if (!asyncResult.isDone()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isRunning() {
            for (AsyncResult asyncResult : asyncResultSet) {
                if (asyncResult.isRunning()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public long getStartTimeMillis() {
            long startTimeMillis = System.currentTimeMillis();
            for (AsyncResult asyncResult : asyncResultSet) {
                startTimeMillis = Math.min(startTimeMillis, asyncResult.getStartTimeMillis());
            }
            return startTimeMillis;
        }

        @Override
        public long getEndTimeMillis() {
            long endTimeMillis = 0;
            for (AsyncResult asyncResult : asyncResultSet) {
                endTimeMillis = Math.max(endTimeMillis, asyncResult.getEndTimeMillis());
            }
            return endTimeMillis;
        }

        @Override
        public long getTotalTimeMillis() {
            return getEndTimeMillis() - getStartTimeMillis();
        }

        @Override
        public void cancel() {
            cancelAll();
        }

        @Override
        public void await() throws InterruptedException {
            for (AsyncResult asyncResult : asyncResultSet) {
                asyncResult.await();
            }
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            throw new UnsupportedOperationException("getHeaders of MergeCallback is not supported");
        }
    }
}
