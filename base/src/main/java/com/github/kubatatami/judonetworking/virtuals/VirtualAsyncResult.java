package com.github.kubatatami.judonetworking.virtuals;

import com.github.kubatatami.judonetworking.AsyncResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Kuba on 09/08/15.
 */
public class VirtualAsyncResult implements AsyncResult {

    private Map<String, List<String>> headers;

    private final long startTimeMillis;

    private final long endTimeMillis;

    public VirtualAsyncResult(Map<String, List<String>> headers) {
        this.headers = headers;
        startTimeMillis = endTimeMillis = System.currentTimeMillis();
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    @Override
    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    @Override
    public long getTotalTimeMillis() {
        return 0;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void await() {

    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}
