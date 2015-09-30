package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

public abstract class WrappedCallback<T, S> implements Callback<T> {

    private Callback<S> outerCallback;

    public WrappedCallback(Callback<S> outerCallback) {
        super();
        this.outerCallback = outerCallback;
    }

    protected Boolean hasCallback() {
        return outerCallback != null;
    }

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        if (hasCallback())
            outerCallback.onStart(cacheInfo, asyncResult);
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
