package com.github.kubatatami.judonetworking.stateful;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.batches.DecoratorBatch;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;

public final class StatefulBatch<T> extends DecoratorBatch<T> implements Stateful<Batch<?>> {

    private AsyncResult asyncResult;

    private final int id;

    private final String who;

    private int progress;

    private boolean consume = false;

    private Object[] data;

    private JudoException exception;

    public StatefulBatch(StatefulController controller, Batch<T> batch) {
        this(controller, batch.getClass().hashCode(), batch);
    }

    public StatefulBatch(StatefulController controller, int id, Batch<T> batch) {
        super(batch);
        this.id = id;
        this.who = controller.getWho();
        StatefulCache.addStatefulCallback(who, id, this);
    }

    @Override
    public final void onStart(AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        consume = false;
        data = null;
        exception = null;
        super.onStart(asyncResult);
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (batch.get() != null) {
            StatefulCache.removeStatefulCallback(who, id);
            consume = true;
        }
    }

    @Override
    public void onProgress(int progress) {
        super.onProgress(progress);
        this.progress = progress;
    }

    @Override
    public void tryCancel() {
        if (asyncResult != null) {
            asyncResult.cancel();
            consume = true;
        }
    }

    public void setCallback(Batch<?> batch) {
        this.batch = new WeakReference<>((Batch<T>) batch);
        if (batch != null) {
            if (progress > 0) {
                batch.onProgress(progress);
            }
            if (!consume) {
                if (data != null) {
                    this.batch.get().onSuccess(data);
                } else if (exception != null) {
                    this.batch.get().onError(exception);
                }
            }
        }
    }

}