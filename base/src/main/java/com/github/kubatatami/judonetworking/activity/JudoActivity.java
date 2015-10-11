package com.github.kubatatami.judonetworking.activity;

import android.app.Activity;

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
public class JudoActivity extends Activity implements StatefulController {

    @Override
    public String getWho() {
        return "activity_" + getTaskId();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            StatefulCache.removeAllStatefulCallbacks(getWho());
        }else{
            StatefulCache.removeAllControllersCallbacks(getWho());
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

    public void cancelRequest(int id){
        StatefulCache.cancelRequest(this, id);
    }
}