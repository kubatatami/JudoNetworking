package com.github.kubatatami.judonetworking;

import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 14.03.2013
 * Time: 13:56
 */
public class AdapterCallback<T> extends Callback<List<T>> {

    private final ArrayAdapter<T> adapter;
    private boolean clear = true;

    public AdapterCallback(ArrayAdapter<T> adapter) {
        this.adapter = adapter;
    }

    public AdapterCallback(ArrayAdapter<T> adapter, boolean clear) {
        this.adapter = adapter;
        this.clear = clear;
    }

    public boolean filter(T result) {
        return true;
    }

    @Override
    public void onFinish(List<T> result) {
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
