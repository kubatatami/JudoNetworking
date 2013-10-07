package com.jsonrpclib;

import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 14.03.2013
 * Time: 13:56
 */
public class JsonAdapterCallback<T> extends JsonCallback<List<T>> {

    private final ArrayAdapter<T> adapter;
    private boolean clear=true;

    public JsonAdapterCallback(ArrayAdapter<T> adapter) {
        this.adapter = adapter;
    }

    public JsonAdapterCallback(ArrayAdapter<T> adapter,boolean clear) {
        this.adapter = adapter;
        this.clear = clear;
    }

    public boolean filtr(T result) {
        return true;
    }

    @Override
    public void onFinish(List<T> result) {
        if(clear)
        {
            adapter.clear();
        }

        adapter.setNotifyOnChange(false);
        for (T object : result) {
            if (filtr(object)) {
                adapter.add(object);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
