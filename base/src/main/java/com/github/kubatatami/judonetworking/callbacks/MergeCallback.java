package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
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

    private JudoException exception;

    private Map<BaseCallback<?>, Integer> progressMap = new HashMap<>();

    private Set<AsyncResult> asyncResultSet = new HashSet<>();

    private Callback<T> finalCallback;

    private T result;

    public MergeCallback(int requests) {
        this.requests = requests;
    }

    public MergeCallback(int requests, Callback<T> finalCallback) {
        this.requests = requests;
        this.finalCallback = finalCallback;
    }

    public final void addStart(AsyncResult asyncResult) {
        if (!started) {
            started = true;
            onMergeStart();
        }
        asyncResultSet.add(asyncResult);
    }

    public final void addProgress(BaseCallback<?> callback, int progress) {
        progressMap.put(callback, Math.min(100, progress));
        onMergeProgress(calculateProgress());
    }

    protected final int calculateProgress() {
        int progress = 0;
        for (int value : progressMap.values()) {
            progress += value;
        }
        return progress / requests;
    }

    public final void addSuccess() {
        finish();
    }

    private void finish() {
        finishRequests++;
        if (finishRequests == requests) {
            if(exception == null) {
                onMergeSuccess();
            } else {
                onMergeError(exception);
            }
            onMergeFinish();
        }
    }

    public final void addError(JudoException e) {
        this.exception = e;
        finish();
    }

    public void setResult(T result) {
        this.result = result;
    }

    protected void onMergeStart() {
        if (finalCallback != null) {
            finalCallback.onStart(null, new MergeAsyncResult());
        }
    }

    protected void onMergeProgress(int progress) {
        if (finalCallback != null) {
            finalCallback.onProgress(progress);
        }
    }

    protected void onMergeSuccess() {
        if (finalCallback != null) {
            finalCallback.onSuccess(result);
        }
    }

    protected void onMergeError(JudoException e) {
        if (finalCallback != null) {
            finalCallback.onError(e);
        }
    }

    protected void onMergeFinish() {
        if (finalCallback != null) {
            finalCallback.onFinish();
        }
    }

    protected class MergeAsyncResult implements AsyncResult {

        boolean canceled = false;

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
        public void cancel() {
            cancelAll();
        }

        private void cancelAll() {
            canceled = true;
            for (AsyncResult asyncResult : asyncResultSet) {
                asyncResult.cancel();
            }
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
