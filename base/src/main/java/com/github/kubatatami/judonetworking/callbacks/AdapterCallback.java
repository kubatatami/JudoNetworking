package com.github.kubatatami.judonetworking.callbacks;

import android.widget.ArrayAdapter;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 14.03.2013
 * Time: 13:56
 */
public class AdapterCallback<T> extends DefaultCallback<List<T>> {

    private final WeakReference<ArrayAdapter<T>> adapter;
    private boolean clear = true;

    public AdapterCallback(ArrayAdapter<T> adapter) {
        this.adapter = new WeakReference<>(adapter);
    }

    public AdapterCallback(ArrayAdapter<T> adapter, boolean clear) {
        this.adapter = new WeakReference<>(adapter);
        this.clear = clear;
    }

    public boolean filter(T result) {
        return true;
    }

    @Override
    public void onSuccess(List<T> result) {
        ArrayAdapter<T> adapter = this.adapter.get();
        if (adapter != null) {
            if (clear) {
                adapter.clear();
            }

            adapter.setNotifyOnChange(false);
            for (T object : result) {
                if (filter(object)) {
                    adapter.add(object);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }
}
