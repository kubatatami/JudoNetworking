package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;

/**
 * Created by Kuba on 19/03/15.
 */
public class DecoratorCallback<T> extends DefaultCallback<T> {

    protected Callback<T> internalCallback;

    protected MergeCallback internalMergeCallback;

    public DecoratorCallback(Callback<T> callback) {
        this.internalCallback = callback;
    }

    public DecoratorCallback(MergeCallback mergeCallback) {
        this.internalMergeCallback = mergeCallback;
    }

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        if (internalCallback != null) {
            internalCallback.onStart(cacheInfo, asyncResult);
        }
        if (internalMergeCallback != null) {
            internalMergeCallback.addStart(asyncResult);
        }
    }

    @Override
    public void onSuccess(T result) {
        if (internalCallback != null) {
            internalCallback.onSuccess(result);
        }
        if (internalMergeCallback != null) {
            internalMergeCallback.addSuccess();
        }
    }

    @Override
    public void onError(JudoException e) {
        if (internalCallback != null) {
            internalCallback.onError(e);
        }
        if (internalMergeCallback != null) {
            internalMergeCallback.addError(e);
        }
    }

    @Override
    public void onFinish() {
        if (internalCallback != null) {
            internalCallback.onFinish();
        }
    }

    @Override
    public void onProgress(int progress) {
        if (internalCallback != null) {
            internalCallback.onProgress(progress);
        }
        if (internalMergeCallback != null) {
            internalMergeCallback.addProgress(this, progress);
        }
    }
}
