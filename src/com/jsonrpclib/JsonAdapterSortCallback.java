package com.jsonrpclib;

import android.os.Build;
import android.widget.ArrayAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 02.04.2013
 * Time: 09:57
 */
public class JsonAdapterSortCallback<T extends Comparable<T>> extends JsonCallback<List<T>> {

    private final ArrayAdapter<T> adapter;

    public JsonAdapterSortCallback(ArrayAdapter<T> adapter) {
        this.adapter = adapter;
    }

    public boolean filtr(T result) {
        return true;
    }

    @Override
    public void onFinish(List<T> result) {
        adapter.clear();
        Collections.sort(result);

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
