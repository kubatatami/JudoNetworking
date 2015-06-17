package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created by Kuba on 19/03/15.
 */
public class DecoratorCallback<T> extends DefaultCallback<T> {
    protected Callback<T> callback;

    public DecoratorCallback(Callback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        if (callback != null) {
            callback.onStart(cacheInfo, asyncResult);
        }
    }

    @Override
    public void onSuccess(T result) {
        if (callback != null) {
            callback.onSuccess(result);
        }
    }

    @Override
    public void onError(JudoException e) {
        if (callback != null) {
            callback.onError(e);
        }
    }

    @Override
    public void onFinish() {
        if (callback != null) {
            callback.onFinish();
        }
    }

    @Override
    public void onProgress(int progress) {
        if (callback != null) {
            callback.onProgress(progress);
        }
    }
}
