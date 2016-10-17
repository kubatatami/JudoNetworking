package com.github.kubatatami.judonetworking.callbacks;

import android.support.v4.app.Fragment;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.04.2013
 * Time: 11:40
 */
public class SupportFragmentCallback<T> extends DefaultCallback<T> {

    private final WeakReference<Fragment> fragment;

    private AsyncResult asyncResult;

    public SupportFragmentCallback(Fragment fragment) {
        this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        if (isFragmentActive()) {
            onSafeStart(cacheInfo, asyncResult);
        } else {
            tryCancel();
        }
    }

    private boolean isFragmentActive() {
        return fragment.get() != null && fragment.get().getActivity() != null && fragment.get().getView() != null;
    }

    @Override
    public final void onSuccess(T result) {
        if (isFragmentActive()) {
            onSafeSuccess(result);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onError(JudoException e) {
        if (isFragmentActive()) {
            onSafeError(e);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onProgress(int progress) {
        if (isFragmentActive()) {
            onSafeProgress(progress);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onFinish() {
        if (isFragmentActive()) {
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
