package com.github.kubatatami.judonetworking.batches;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

public abstract class WrappedBatchCallback<T, S> implements Batch<T> {

    private Callback<S> outerCallback;

    public WrappedBatchCallback(Callback<S> outerCallback) {
        super();
        this.outerCallback = outerCallback;
    }

    protected Boolean hasCallback() {
        return outerCallback != null;
    }

    @Override
    public void onStart(AsyncResult asyncResult) {
        if (hasCallback())
            outerCallback.onStart(new CacheInfo(false, 0L), asyncResult);
    }

    @Override
    public void run(T api) {

    }

    @Override
    public void runNonFatal(T api) {

    }

    @Override
    public void onError(JudoException e) {
        if (hasCallback()) outerCallback.onError(e);
    }

    @Override
    public void onFinish() {
        if (hasCallback()) outerCallback.onFinish();
    }

    @Override
    public void onProgress(int progress) {
        if (hasCallback()) outerCallback.onProgress(progress);
    }


}
