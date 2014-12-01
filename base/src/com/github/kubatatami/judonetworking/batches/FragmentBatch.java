package com.github.kubatatami.judonetworking.batches;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;

import com.github.kubatatami.judonetworking.internals.AsyncResult;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class FragmentBatch<T> implements BatchInterface<T>, FragmentManager.OnBackStackChangedListener{


    private final Fragment fragment;
    private AsyncResult asyncResult;
    private FragmentManager manager;

    public FragmentBatch(Fragment fragment) {
        this.fragment = fragment;
        this.manager = fragment.getFragmentManager();
        if(manager!=null) {
            manager.addOnBackStackChangedListener(this);
        }
    }


    @Override
    public void onBackStackChanged() {
        if (fragment.getActivity() == null) {
            tryCancel();
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


    @Override
    public final void onStart(AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        if (fragment.getActivity() != null) {
            onSafeStart(asyncResult);
        } else {
            tryCancel();
        }
    }

    @Override
    public void run(final T api) {

    }

    @Override
    public void runNonFatal(final T api) {
    }

    @Override
    public final void onSuccess(Object[] results) {
        if (fragment.getActivity() != null) {
            onSafeSuccess(results);
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


    public void onSafeStart(AsyncResult asyncResult) {

    }


    public void onSafeSuccess(Object[] results) {
    }


    public void onSafeError(JudoException e) {

    }


    public void onSafeProgress(int progress) {

    }


    public void onSafeFinish() {

    }
}
