package com.github.kubatatami.judonetworking.stateful;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.callbacks.AsyncResultCallback;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.callbacks.DecoratorCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;


public final class StatefulCallback<T> extends DecoratorCallback<T> implements Stateful<Callback<T>> {

    private AsyncResult asyncResult;

    private final int id;

    private final String who;

    private int progress;

    private boolean consume = false;

    private boolean finishedSuccessfully = false;

    private T data;

    private JudoException exception;

    public StatefulCallback(StatefulController controller, Callback<T> callback, boolean active) {
        this(controller, StatefulCache.getCallbackId(callback), callback, active);
    }

    public StatefulCallback(StatefulController controller, int id, Callback<T> callback, boolean active) {
        super(active ? callback : null);
        this.id = id;
        this.who = controller.getWho();
        StatefulCache.addStatefulCallback(who, id, this);
    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        consume = false;
        finishedSuccessfully = false;
        data = null;
        exception = null;
        super.onStart(cacheInfo, asyncResult);
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (internalCallback != null) {
            StatefulCache.endStatefulCallback(who, id);
            consume = true;
        }
    }

    @Override
    public void onProgress(int progress) {
        super.onProgress(progress);
        this.progress = progress;
    }

    @Override
    public void onSuccess(T result) {
        this.data = result;
        this.finishedSuccessfully = true;
        super.onSuccess(result);
    }

    @Override
    public void tryCancel() {
        if (asyncResult != null) {
            asyncResult.cancel();
            consume = true;
        }
    }

    @Override
    public void setCallback(Callback<T> callback) {
        this.internalCallback = callback;
        if (callback != null) {
            if (callback instanceof AsyncResultCallback) {
                ((AsyncResultCallback) callback).setAsyncResult(asyncResult);
            }
            if (progress > 0) {
                callback.onProgress(progress);
            }
            if (!consume) {
                if (finishedSuccessfully) {
                    this.internalCallback.onSuccess(data);
                    onFinish();
                } else if (exception != null) {
                    this.internalCallback.onError(exception);
                    onFinish();
                }
            }
        }
    }

}