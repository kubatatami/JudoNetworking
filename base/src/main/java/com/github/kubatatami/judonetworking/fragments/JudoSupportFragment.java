package com.github.kubatatami.judonetworking.fragments;

import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

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
public class JudoSupportFragment extends DialogFragment implements StatefulController {

    private String mWho;
    private Handler handler = new Handler();

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

    public void cancelRequest(int id){
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