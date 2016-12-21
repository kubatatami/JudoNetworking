package com.github.kubatatami.judonetworking.callbacks;

import android.view.View;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.builders.DefaultCallbackBuilder;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created by Kuba on 23/05/14.
 */
public class ViewCallback<T> extends DefaultCallbackBuilder.LambdaCallback<T> {

    private CallbackCache callbackCache;

    public ViewCallback(Builder<T> builder) {
        super(builder);
        this.callbackCache = new CallbackCache(builder.view, this);
    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        setAsyncResult(asyncResult);
        if (!callbackCache.cancel()) {
            super.onStart(cacheInfo, asyncResult);
        }
    }

    @Override
    public final void onSuccess(T result) {
        if (!callbackCache.cancel()) {
            super.onSuccess(result);
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
        if (callbackCache.consume()) {
            super.onFinish();
        }
    }

    public static void cancelRequest(View view) {
        CallbackCache.cancelRequest(view);
    }

    public static class Builder<T> extends DefaultCallbackBuilder<T, Builder<T>> {

        private View view;

        public Builder(View view) {
            this.view = view;
        }

        @Override
        public ViewCallback<T> build() {
            return new ViewCallback<>(this);
        }
    }

}
