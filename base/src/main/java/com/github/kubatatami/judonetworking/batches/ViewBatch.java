package com.github.kubatatami.judonetworking.batches;

import android.view.View;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.builders.DefaultBatchBuilder;
import com.github.kubatatami.judonetworking.callbacks.CallbackCache;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created by Kuba on 23/05/14.
 */
public class ViewBatch<T> extends DefaultBatchBuilder.LambdaBatch<T> {

    private CallbackCache callbackCache;

    public ViewBatch(ViewBatch.Builder<T> builder) {
        super(builder);
        this.callbackCache = new CallbackCache(builder.view, this);
    }

    @Override
    public final void onStart(AsyncResult asyncResult) {
        if (!callbackCache.cancel()) {
            super.onStart(asyncResult);
        }
    }

    @Override
    public final void onSuccess(Object[] results) {
        if (!callbackCache.cancel()) {
            super.onSuccess(results);
        }
    }

    @Override
    public final void onError(JudoException e) {
        if (!callbackCache.cancel()) {
            super.onError(e);
        }
    }

    @Override
    public final void onProgress(int progress) {
        if (!callbackCache.cancel()) {
            super.onProgress(progress);
        }
    }

    @Override
    public final void onFinish() {
        super.onFinish();
        if (callbackCache.consume()) {
            super.onFinish();
        }
    }

    public static void cancelRequest(View view) {
        CallbackCache.cancelRequest(view);
    }

    public static class Builder<T> extends DefaultBatchBuilder<T, Builder<T>> {

        private View view;

        public Builder(View view) {
            this.view = view;
        }

        @Override
        public ViewBatch<T> build() {
            return new ViewBatch<>(this);
        }
    }

}
