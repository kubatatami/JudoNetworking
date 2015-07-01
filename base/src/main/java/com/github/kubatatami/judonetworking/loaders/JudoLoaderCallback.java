package com.github.kubatatami.judonetworking.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created by Kuba on 01/07/15.
 */
public abstract class JudoLoaderCallback<T> implements LoaderManager.LoaderCallbacks<T>, Callback<T> {

    private final JudoLoader<T, ?> loader;

    public JudoLoaderCallback(JudoLoader<T, ?> loader) {
        this.loader = loader;
    }

    @Override
    public final Loader<T> onCreateLoader(int id, Bundle args) {
        return loader;
    }

    @Override
    public final void onLoadFinished(Loader<T> l, T data) {
        if(loader.getException() !=null){
            onError(loader.getException());
        }else{
            onSuccess(data);
        }
        onFinish();
    }

    @Override
    public final void onLoaderReset(Loader<T> loader) {

    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {

    }

    @Override
    public final void onProgress(int progress) {

    }

    @Override
    public void onSuccess(T result){

    }

    @Override
    public void onError(JudoException e){

    }

    @Override
    public void onFinish(){

    }
}
