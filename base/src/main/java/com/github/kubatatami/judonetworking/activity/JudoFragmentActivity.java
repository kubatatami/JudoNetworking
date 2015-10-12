package com.github.kubatatami.judonetworking.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

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
public class JudoFragmentActivity extends FragmentActivity implements StatefulController {

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            id = JudoActivity.generateId(this);
        }else{
            id = savedInstanceState.getString(JudoActivity.UNIQUE_ACTIVITY_ID);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(JudoActivity.UNIQUE_ACTIVITY_ID, id);
    }

    @Override
    public String getWho() {
        return "activity_" + id;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            StatefulCache.removeAllStatefulCallbacks(getWho());
        } else {
            StatefulCache.removeAllControllersCallbacks(getWho());
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
}