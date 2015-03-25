package com.github.kubatatami.judonetworking.callbacks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;

/**
 * Created by Kuba on 13/12/14.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class ActivityCallback<T> extends DefaultCallback<T>  {
    private final WeakReference<Activity> activity;
    private AsyncResult asyncResult;

    public ActivityCallback(Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        if (isActive()) {
            onSafeStart(cacheInfo, asyncResult);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onSuccess(T result) {
        if (isActive()) {
            onSafeSuccess(result);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onError(JudoException e) {
        if (isActive()) {
            onSafeError(e);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onProgress(int progress) {
        if (isActive()) {
            onSafeProgress(progress);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onFinish() {
        if (isActive()) {
            onSafeFinish();
        } else {
            tryCancel();
        }
    }

    protected void tryCancel() {
        if (asyncResult != null) {
            asyncResult.cancel();
        }
    }


    protected boolean isActive(){
        return activity.get() != null && !activity.get().isFinishing();
    }

    public void onSafeStart(CacheInfo cacheInfo, AsyncResult asyncResult) {

    }

    public void onSafeProgress(int progress) {

    }

    public void onSafeSuccess(T result) {

    }

    public void onSafeFinish() {

    }

    public void onSafeError(JudoException e) {

    }
}
