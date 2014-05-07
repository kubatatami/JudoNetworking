package com.github.kubatatami.judonetworking;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.04.2013
 * Time: 11:40
 */
public class SupportFragmentCallback<T> extends Callback<T> implements FragmentManager.OnBackStackChangedListener {

    private final Fragment fragment;
    private AsyncResult asyncResult;
    private FragmentManager manager;

    public SupportFragmentCallback(Fragment fragment) {
        this.fragment = fragment;
        this.manager = fragment.getFragmentManager();
        manager.addOnBackStackChangedListener(this);
    }

    @Override
    public final void onStart(boolean isCached, AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        if (fragment.getActivity() != null) {
            onSafeStart(isCached, asyncResult);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onSuccess(T result) {
        if (fragment.getActivity() != null) {
            onSafeSuccess(result);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onError(JudoException e) {
        if (fragment.getActivity() != null) {
            onSafeError(e);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onProgress(int progress) {
        if (fragment.getActivity() != null) {
            onSafeProgress(progress);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onFinish() {
        if (fragment.getActivity() != null) {
            onSafeFinish();
        } else {
            tryCancel();
        }
        manager.removeOnBackStackChangedListener(this);
    }

    protected void tryCancel() {
        if (asyncResult != null) {
            asyncResult.cancel();
            manager.removeOnBackStackChangedListener(this);
        }
    }

    public void onSafeStart(boolean isCached, AsyncResult asyncResult) {

    }

    public void onSafeProgress(int progress) {

    }

    public void onSafeSuccess(T result) {

    }

    public void onSafeFinish() {

    }

    public void onSafeError(JudoException e) {

    }


    @Override
    public void onBackStackChanged() {
        if (fragment.getActivity() == null) {
            tryCancel();
        }
    }
}
