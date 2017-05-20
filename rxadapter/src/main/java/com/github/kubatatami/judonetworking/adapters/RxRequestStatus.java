package com.github.kubatatami.judonetworking.adapters;

import com.github.kubatatami.judonetworking.AsyncResult;

import java.util.List;
import java.util.Map;

public class RxRequestStatus<T> implements AsyncResult {

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

    @Override
    public boolean isDone() {
        return asyncResult.isDone();
    }

    @Override
    public boolean isCancelled() {
        return asyncResult.isCancelled();
    }

    @Override
    public boolean isRunning() {
        return asyncResult.isRunning();
    }

    @Override
    public long getStartTimeMillis() {
        return asyncResult.getStartTimeMillis();
    }

    @Override
    public long getEndTimeMillis() {
        return asyncResult.getEndTimeMillis();
    }

    @Override
    public long getTotalTimeMillis() {
        return asyncResult.getTotalTimeMillis();
    }

    @Override
    public void cancel() {
        asyncResult.cancel();
    }

    @Override
    public void await() throws InterruptedException {
        asyncResult.await();
    }

    @Override
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
