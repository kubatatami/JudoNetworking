package com.github.kubatatami.judonetworking.virtuals;

import com.github.kubatatami.judonetworking.AsyncResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Kuba on 09/08/15.
 */
public class VirtualAsyncResult implements AsyncResult {

    Map<String, List<String>> headers;

    public VirtualAsyncResult(Map<String, List<String>> headers) {
        this.headers = headers;
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
