package com.github.kubatatami.judonetworking.batches;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.callbacks.AsyncResultCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public abstract class DefaultBatch<T> implements Batch<T>, AsyncResultCallback {

    MergeCallback mergeCallback;
    private AsyncResult asyncResult;

    public DefaultBatch() {
    }

    public DefaultBatch(MergeCallback mergeCallback) {
        this.mergeCallback = mergeCallback;
    }

    @Override
    public void onStart(AsyncResult asyncResult) {
        if (mergeCallback != null) {
            mergeCallback.addStart(asyncResult);
        }
    }

    @Override
    public void run(final T api) {

    }

    @Override
    public void runNonFatal(final T api) {
    }

    @Override
    public void onSuccess(Object[] results) {
        if (mergeCallback != null) {
            mergeCallback.addSuccess();
        }
    }

    @Override
    public void onError(JudoException e) {
        if (mergeCallback != null) {
            mergeCallback.addError(e);
        }
    }

    @Override
    public void onProgress(int progress) {
        if (mergeCallback != null) {
            mergeCallback.addProgress(this, progress);
        }
    }

    @Override
    public void onFinish() {

    }

    @Override
    public final AsyncResult getAsyncResult() {
        return asyncResult;
    }

    @Override
    public final void setAsyncResult(AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
    }
}
