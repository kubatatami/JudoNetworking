package com.github.kubatatami.judonetworking.batches;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;

/**
 * Created by Kuba on 02/07/15.
 */
public class DecoratorBatch<T> implements Batch<T> {

    protected WeakReference<Batch<T>> batch;

    public DecoratorBatch(Batch<T> batch) {
        this.batch = new WeakReference<>(batch);
    }

    @Override
    public void onStart(AsyncResult asyncResult) {
        if (batch.get() != null) {
            batch.get().onStart(asyncResult);
        }
    }

    @Override
    public void run(T api) {
        if (batch.get() != null) {
            batch.get().run(api);
        }
    }

    @Override
    public void runNonFatal(T api) {
        if (batch.get() != null) {
            batch.get().runNonFatal(api);
        }
    }

    @Override
    public void onProgress(int progress) {
        if (batch.get() != null) {
            batch.get().onProgress(progress);
        }
    }

    @Override
    public void onSuccess(Object[] result) {
        if (batch.get() != null) {
            batch.get().onSuccess(result);
        }
    }

    @Override
    public void onError(JudoException e) {
        if (batch.get() != null) {
            batch.get().onError(e);
        }
    }

    @Override
    public void onFinish() {
        if (batch.get() != null) {
            batch.get().onFinish();
        }
    }
}
