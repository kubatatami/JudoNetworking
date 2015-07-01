package com.github.kubatatami.judonetworking.loaders;

import android.content.Context;
import android.support.v4.content.Loader;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.callbacks.DefaultCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created by Kuba on 01/07/15.
 */
public abstract class JudoLoader<T,Z> extends Loader<T> {
    /**
     * Stores away the application context associated with context. Since Loaders can be used
     * across multiple activities it's dangerous to store the context directly.
     *
     * @param context used to retrieve the application context.
     */

    private final Z apiService;
    private AsyncResult asyncResult;
    private JudoException exception;

    private Callback<T> callback = new DefaultCallback<T>(){
        @Override
        public void onSuccess(T result) {
            deliverResult(result);
        }

        @Override
        public void onError(JudoException e) {
            exception=e;
            deliverResult(null);
        }

        @Override
        public void onFinish() {
            super.onFinish();
        }
    };

    public JudoLoader(Context context, Z apiService) {
        super(context);
        this.apiService=apiService;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if(asyncResult!=null){
            asyncResult.cancel();
        }
        asyncResult=doRequest(apiService, callback);
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        onStartLoading();
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        if(asyncResult!=null){
            asyncResult.cancel();
        }
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        if(asyncResult!=null){
            asyncResult.cancel();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        if(asyncResult!=null){
            asyncResult.cancel();
        }
    }

    JudoException getException() {
        return exception;
    }

    protected abstract AsyncResult doRequest(Z api, Callback<T> callback);
}
