package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;

/**
 * Created by Kuba on 19/03/15.
 */
public class DecoratorCallback<T> extends DefaultCallback<T> {
    protected WeakReference<Callback<T>> callback;

    public DecoratorCallback(Callback<T> callback) {
        this.callback = new WeakReference<>(callback);
    }

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        if (callback.get() != null) {
            callback.get().onStart(cacheInfo, asyncResult);
        }
    }

    @Override
    public void onSuccess(T result) {
        if (callback.get() != null) {
            callback.get().onSuccess(result);
        }
    }

    @Override
    public void onError(JudoException e) {
        if (callback.get() != null) {
            callback.get().onError(e);
        }
    }

    @Override
    public void onFinish() {
        if (callback.get() != null) {
            callback.get().onFinish();
        }
    }

    @Override
    public void onProgress(int progress) {
        if (callback.get() != null) {
            callback.get().onProgress(progress);
        }
    }
}
