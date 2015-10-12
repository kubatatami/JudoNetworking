package com.github.kubatatami.judonetworking.fragments;


import android.app.Activity;
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
        if (getActivity().isFinishing()) {
            StatefulCache.removeAllStatefulCallbacks(getWho());
        }
    }

    protected boolean connectCallback(BaseCallback<?>... callbacks) {
        return StatefulCache.connectControllerCallbacks(this, callbacks);
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

    static <T, Z extends T> String getFragmentWho(Activity activity, Class<T> clazz, Z object){
        try {
            if(!(activity instanceof StatefulController)){
                throw new RuntimeException("Activity must be instance of JudoActivity.");
            }
            Field whoFiled = clazz.getDeclaredField("mWho");
            whoFiled.setAccessible(true);
            return ((StatefulController)activity).getWho() + whoFiled.get(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getWho() {
        if (mWho == null) {
            mWho = getFragmentWho(getActivity(),Fragment.class, this);
        }
        return mWho;
    }

}