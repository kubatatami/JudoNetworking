package com.github.kubatatami.judonetworking.fragments;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.github.kubatatami.judonetworking.CallbacksConnector;
import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.stateful.StatefulBatch;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;
import com.github.kubatatami.judonetworking.stateful.StatefulCallback;
import com.github.kubatatami.judonetworking.stateful.StatefulController;

/**
 * Created by Kuba on 01/07/15.
 */
public abstract class JudoSupportFragment extends DialogFragment implements StatefulController {

    private String mWho;

    private boolean active;

    @Override
    public void onResume() {
        super.onResume();
        active = true;
        onConnectCallbacks(new CallbacksConnector(this));
    }

    @Override
    public void onPause() {
        super.onPause();
        active = false;
        StatefulCache.removeAllControllersCallbacks(getWho());
    }

    @Override
    public void onConnectCallbacks(CallbacksConnector connector) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isFinishing()) {
            StatefulCache.removeAllStatefulCallbacks(getWho());
        }
    }

    protected <T> StatefulCallback<T> generateCallback(Callback<T> callback) {
        return new StatefulCallback<>(this, callback, active);
    }

    protected <T> StatefulCallback<T> generateCallback(int id, Callback<T> callback) {
        return new StatefulCallback<>(this, id, callback, active);
    }

    protected <T> StatefulBatch<T> generateCallback(Batch<T> batch) {
        return new StatefulBatch<>(this, batch, active);
    }

    protected <T> StatefulBatch<T> generateCallback(int id, Batch<T> batch) {
        return new StatefulBatch<>(this, id, batch, active);
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