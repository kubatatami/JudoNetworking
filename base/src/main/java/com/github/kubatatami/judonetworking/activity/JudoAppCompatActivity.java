package com.github.kubatatami.judonetworking.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
public abstract class JudoAppCompatActivity extends AppCompatActivity implements StatefulController {

    private String id;

    private boolean active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            id = JudoActivity.generateId(this);
        } else {
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
        }
    }

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
}