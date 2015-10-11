package com.github.kubatatami.judonetworking.batches;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;

/**
 * Created by Kuba on 02/07/15.
 */
public class DecoratorBatch<T> implements Batch<T> {

    protected Batch<T> batch;

    public DecoratorBatch(Batch<T> batch) {
        this.batch = batch;
    }

    @Override
    public void onStart(AsyncResult asyncResult) {
        if (batch != null) {
            batch.onStart(asyncResult);
        }
    }

    @Override
    public void run(T api) {
        if (batch != null) {
            batch.run(api);
        }
    }

    @Override
    public void runNonFatal(T api) {
        if (batch != null) {
            batch.runNonFatal(api);
        }
    }

    @Override
    public void onProgress(int progress) {
        if (batch != null) {
            batch.onProgress(progress);
        }
    }

    @Override
    public void onSuccess(Object[] result) {
        if (batch != null) {
            batch.onSuccess(result);
        }
    }

    @Override
    public void onError(JudoException e) {
        if (batch != null) {
            batch.onError(e);
        }
    }

    @Override
    public void onFinish() {
        if (batch != null) {
            batch.onFinish();
        }
    }
}
