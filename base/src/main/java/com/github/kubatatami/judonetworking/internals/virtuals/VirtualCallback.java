package com.github.kubatatami.judonetworking.internals.virtuals;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.results.ErrorResult;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.internals.results.RequestSuccessResult;

public class VirtualCallback implements Callback {
    private int id;
    private RequestResult result;
    private AsyncResult asyncResult;

    public VirtualCallback(int id) {
        this.id = id;
    }

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
    }

    @Override
    public void onSuccess(Object result) {
        this.result = new RequestSuccessResult(id, result);
    }

    @Override
    public void onError(JudoException e) {
        this.result = new ErrorResult(id, e);
    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onProgress(int progress) {
        throw new IllegalAccessError("Virtual server can't invoke onProgress");
    }

    public RequestResult getResult() {
        return result;
    }

    public AsyncResult getAsyncResult() {
        return asyncResult;
    }
}