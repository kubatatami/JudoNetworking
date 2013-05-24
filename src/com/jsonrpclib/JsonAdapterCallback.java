package com.jsonrpclib;

import android.os.Build;
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

    public JsonAdapterCallback(ArrayAdapter<T> adapter) {
        this.adapter = adapter;
    }

    public boolean filtr(T result) {
        return true;
    }

    @Override
    public void onFinish(List<T> result) {
        adapter.clear();

        adapter.setNotifyOnChange(false);
        for (T object : result) {
            if(filtr(object))
            {
                adapter.add(object);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
