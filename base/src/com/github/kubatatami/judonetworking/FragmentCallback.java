package com.github.kubatatami.judonetworking;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.04.2013
 * Time: 11:40
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentCallback<T> extends Callback<T> implements FragmentManager.OnBackStackChangedListener {

    private final Fragment fragment;
    private AsyncResult asyncResult;
    private FragmentManager manager;

    public FragmentCallback(Fragment fragment) {
        this.fragment = fragment;
        this.manager = fragment.getFragmentManager();
        if(manager!=null) {
            manager.addOnBackStackChangedListener(this);
        }
    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        if (fragment.getActivity() != null) {
            onSafeStart(cacheInfo, asyncResult);
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
        if(manager!=null) {
            manager.removeOnBackStackChangedListener(this);
        }
    }

    protected void tryCancel() {
        if (asyncResult != null) {
            asyncResult.cancel();
            if(manager!=null) {
                manager.removeOnBackStackChangedListener(this);
            }
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


    @Override
    public void onBackStackChanged() {
        if (fragment.getActivity() == null) {
            tryCancel();
        }
    }
}
