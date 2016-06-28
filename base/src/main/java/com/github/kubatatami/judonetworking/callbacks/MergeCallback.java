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

    int requests;

    int finishRequests = 0;

    boolean started = false;

    boolean canceled = false;

    Map<BaseCallback<?>, Integer> progressMap = new HashMap<>();

    Set<AsyncResult> asyncResultSet = new HashSet<>();

    Callback<T> finalCallback;

    T result;

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

    protected final int calculateProgress() {
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
