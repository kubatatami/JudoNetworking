package com.github.kubatatami.judonetworking.fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;

import com.github.kubatatami.judonetworking.CallbacksConnector;
import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.stateful.StatefulBatch;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;
import com.github.kubatatami.judonetworking.stateful.StatefulCallback;
import com.github.kubatatami.judonetworking.stateful.StatefulController;

import java.lang.reflect.Field;

/**
 * Created by Kuba on 01/07/15.
 */
public abstract class JudoFragment extends DialogFragment implements StatefulController {

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

    static <T, Z extends T> String getFragmentWho(Activity activity, Class<T> clazz, Z object) {
        try {
            if (!(activity instanceof StatefulController)) {
                throw new RuntimeException("Activity must be instance of StatefulController eg. JudoActivity.");
            }
            Field whoFiled = clazz.getDeclaredField("mWho");
            whoFiled.setAccessible(true);
            return ((StatefulController) activity).getWho() + whoFiled.get(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getWho() {
        if (mWho == null) {
            mWho = getFragmentWho(getActivity(), Fragment.class, this);
        }
        return mWho;
    }

}