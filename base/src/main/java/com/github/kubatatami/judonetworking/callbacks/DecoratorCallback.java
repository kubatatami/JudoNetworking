package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;

/**
 * Created by Kuba on 19/03/15.
 */
public class DecoratorCallback<T> extends DefaultCallback<T> {

    protected WeakReference<Callback<T>> internalCallback;

    protected WeakReference<MergeCallback> internalMergeCallback;

    public DecoratorCallback(Callback<T> callback) {
        this.internalCallback = new WeakReference<>(callback);
        this.internalMergeCallback = new WeakReference<>(null);
    }

    public DecoratorCallback(MergeCallback mergeCallback) {
        this.internalCallback = new WeakReference<>(null);
        this.internalMergeCallback = new WeakReference<>(mergeCallback);
    }

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        if (internalCallback.get() != null) {
            internalCallback.get().onStart(cacheInfo, asyncResult);
        }
        if (internalMergeCallback.get() != null) {
            internalMergeCallback.get().addStart(asyncResult);
        }
    }

    @Override
    public void onSuccess(T result) {
        if (internalCallback.get() != null) {
            internalCallback.get().onSuccess(result);
        }
        if (internalMergeCallback.get() != null) {
            internalMergeCallback.get().addSuccess();
        }
    }

    @Override
    public void onError(JudoException e) {
        if (internalCallback.get() != null) {
            internalCallback.get().onError(e);
        }
        if (internalMergeCallback.get() != null) {
            internalMergeCallback.get().addError(e);
        }
    }

    @Override
    public void onFinish() {
        if (internalCallback.get() != null) {
            internalCallback.get().onFinish();
        }
    }

    @Override
    public void onProgress(int progress) {
        if (internalCallback.get() != null) {
            internalCallback.get().onProgress(progress);
        }
        if (internalMergeCallback.get() != null) {
            internalMergeCallback.get().addProgress(this, progress);
        }
    }
}
