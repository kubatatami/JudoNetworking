package com.github.kubatatami.judonetworking.adapters;

import com.github.kubatatami.judonetworking.AsyncResult;

import java.util.List;
import java.util.Map;

public class RxRequestStatus<T> {

    private int progress;

    private T result;

    private AsyncResult asyncResult;

    public RxRequestStatus(AsyncResult asyncResult, int progress) {
        this.asyncResult = asyncResult;
        this.progress = progress;
    }

    public RxRequestStatus(AsyncResult asyncResult, T result) {
        this.asyncResult = asyncResult;
        this.result = result;
    }

    public boolean isDone() {
        return result != null;
    }

    public boolean isCancelled() {
        return asyncResult.isCancelled();
    }

    public boolean isRunning() {
        return asyncResult.isRunning();
    }

    public long getStartTimeMillis() {
        return asyncResult.getStartTimeMillis();
    }

    public long getEndTimeMillis() {
        return asyncResult.getEndTimeMillis();
    }

    public long getTotalTimeMillis() {
        return asyncResult.getTotalTimeMillis();
    }

    public void cancel() {
        asyncResult.cancel();
    }

    public Map<String, List<String>> getHeaders() {
        return asyncResult.getHeaders();
    }

    public int getProgress() {
        return progress;
    }

    public T getResult() {
        return result;
    }
}
