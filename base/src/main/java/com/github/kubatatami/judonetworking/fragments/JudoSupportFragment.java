package com.github.kubatatami.judonetworking.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.callbacks.BaseCallback;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.stateful.StatefulBatch;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;
import com.github.kubatatami.judonetworking.stateful.StatefulCallback;
import com.github.kubatatami.judonetworking.stateful.StatefulController;

/**
 * Created by Kuba on 01/07/15.
 */
public class JudoSupportFragment extends DialogFragment implements StatefulController {

    private String mWho;
    private boolean destroyedView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        destroyedView = false;
    }

    @Override
    public void onDestroyView() {
        destroyedView = true;
        super.onDestroyView();
        StatefulCache.removeAllControllersCallbacks(getWho());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isFinishing()) {
            StatefulCache.removeAllStatefulCallbacks(getWho());
        }
    }

    protected boolean connectCallback(BaseCallback<?>... callbacks) {
        destroyedView = false;
        return StatefulCache.connectControllerCallbacks(this, callbacks);
    }

    protected boolean connectCallback(int id, BaseCallback<?> callback) {
        destroyedView = false;
        return StatefulCache.connectControllerCallback(this, id, callback);
    }

    protected <T> StatefulCallback<T> generateCallback(Callback<T> callback) {
        return new StatefulCallback<>(this, callback, destroyedView);
    }

    protected <T> StatefulCallback<T> generateCallback(int id, Callback<T> callback) {
        return new StatefulCallback<>(this, id, callback, destroyedView);
    }

    protected <T> StatefulBatch<T> generateCallback(Batch<T> batch) {
        return new StatefulBatch<>(this, batch, destroyedView);
    }

    protected <T> StatefulBatch<T> generateCallback(int id, Batch<T> batch) {
        return new StatefulBatch<>(this, id, batch, destroyedView);
    }

    public void cancelRequest(int id) {
        StatefulCache.cancelRequest(this, id);
    }

    @Override
    public String getWho() {
        if (mWho == null) {
            mWho = JudoFragment.getFragmentWho(getActivity(), Fragment.class, this);
        }
        return mWho;
    }

}