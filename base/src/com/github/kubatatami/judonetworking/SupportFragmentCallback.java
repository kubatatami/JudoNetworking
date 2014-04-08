package com.github.kubatatami.judonetworking;

import android.support.v4.app.Fragment;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.04.2013
 * Time: 11:40
 */
public class SupportFragmentCallback<T> extends Callback<T> {

    private final Fragment fragment;

    public SupportFragmentCallback(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public final void onStart() {
        if (fragment.getActivity() != null) {
            onSafeStart();
        }
    }

    @Override
    public final void onSuccess(T result) {
        if (fragment.getActivity() != null) {
            onSafeSuccess(result);
        }
    }

    @Override
    public final void onError(JudoException e) {
        if (fragment.getActivity() != null) {
            onSafeError(e);
        }
    }

    @Override
    public final void onProgress(int progress) {
        if (fragment.getActivity() != null) {
            onSafeProgress(progress);
        }
    }

    @Override
    public final void onFinish() {
        if (fragment.getActivity() != null) {
            onSafeFinish();
        }
    }

    public void onSafeStart() {

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
