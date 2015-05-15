package com.github.kubatatami.judonetworking.callbacks;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;

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
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentCallback<T> extends DefaultCallback<T> implements FragmentManager.OnBackStackChangedListener {

    private final WeakReference<Fragment> fragment;
    private final WeakReference<FragmentManager> manager;
    private AsyncResult asyncResult;

    public FragmentCallback(Fragment fragment) {
        this.fragment = new WeakReference<>(fragment);
        this.manager = new WeakReference<>(fragment.getFragmentManager());
        if (manager.get() != null) {
            manager.get().addOnBackStackChangedListener(this);
        }
    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        if (fragment.get() != null && fragment.get().getActivity() != null) {
            onSafeStart(cacheInfo, asyncResult);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onSuccess(T result) {
        if (fragment.get() != null && fragment.get().getActivity() != null) {
            onSafeSuccess(result);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onError(JudoException e) {
        if (fragment.get() != null && fragment.get().getActivity() != null) {
            onSafeError(e);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onProgress(int progress) {
        if (fragment.get() != null && fragment.get().getActivity() != null) {
            onSafeProgress(progress);
        } else {
            tryCancel();
        }
    }

    @Override
    public final void onFinish() {
        if (fragment.get() != null && fragment.get().getActivity() != null) {
            onSafeFinish();
        } else {
            tryCancel();
        }
        if (manager.get() != null) {
            manager.get().removeOnBackStackChangedListener(this);
        }
    }

    protected void tryCancel() {
        if (asyncResult != null) {
            asyncResult.cancel();
            if (manager.get() != null) {
                manager.get().removeOnBackStackChangedListener(this);
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
        if (fragment.get() != null && fragment.get().getActivity() == null) {
            tryCancel();
        }
    }
}
