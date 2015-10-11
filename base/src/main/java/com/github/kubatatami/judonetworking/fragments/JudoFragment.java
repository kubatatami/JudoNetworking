package com.github.kubatatami.judonetworking.fragments;


import android.app.DialogFragment;
import android.app.Fragment;

import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.callbacks.BaseCallback;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.stateful.StatefulBatch;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;
import com.github.kubatatami.judonetworking.stateful.StatefulCallback;
import com.github.kubatatami.judonetworking.stateful.StatefulController;

import java.lang.reflect.Field;

/**
 * Created by Kuba on 01/07/15.
 */
public class JudoFragment extends DialogFragment implements StatefulController {

    private String mWho;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        StatefulCache.removeAllControllersCallbacks(getWho());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRemoving()) {
            StatefulCache.removeAllStatefulCallbacks(getWho());
        }
    }

    protected boolean connectCallback(BaseCallback<?> callback) {
        return connectCallback(callback.getClass().hashCode(), callback);
    }

    protected boolean connectCallback(int id, BaseCallback<?> callback) {
        return StatefulCache.connectControllerCallback(this, id, callback);
    }

    protected <T> StatefulCallback<T> generateCallback(Callback<T> callback) {
        return new StatefulCallback<>(this, callback);
    }

    protected <T> StatefulCallback<T> generateCallback(int id, Callback<T> callback) {
        return new StatefulCallback<>(this, id, callback);
    }

    protected <T> StatefulBatch<T> generateCallback(Batch<T> batch) {
        return new StatefulBatch<>(this, batch);
    }

    protected <T> StatefulBatch<T> generateCallback(int id, Batch<T> batch) {
        return new StatefulBatch<>(this, id, batch);
    }

    public void cancelRequest(int id) {
        StatefulCache.cancelRequest(this, id);
    }

    @Override
    public String getWho() {
        if (mWho == null) {
            try {
                Field whoFiled = Fragment.class.getDeclaredField("mWho");
                whoFiled.setAccessible(true);
                mWho = (String) whoFiled.get(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return "activity_" + getActivity().getTaskId() + "_fragment_" + mWho;
    }

}