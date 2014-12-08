package com.github.kubatatami.judonetworking.observers;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.internals.cache.CacheInfo;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.callbacks.CallbackInterface;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created by Kuba on 17/03/14.
 */
public class CallbackWrapper<T> extends Callback<T> {

    protected CallbackInterface<T> baseCallback;

    public CallbackWrapper(CallbackInterface<T> baseCallback) {
        this.baseCallback = baseCallback;
    }

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        baseCallback.onStart(cacheInfo,asyncResult);
    }

    @Override
    public void onSuccess(T result) {
        baseCallback.onSuccess(result);
    }

    @Override
    public void onError(JudoException e) {
        baseCallback.onError(e);
    }

    @Override
    public void onFinish() {
        baseCallback.onFinish();
    }

    @Override
    public void onProgress(int progress) {
        baseCallback.onProgress(progress);
    }

}
