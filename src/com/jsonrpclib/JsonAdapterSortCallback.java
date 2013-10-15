package com.jsonrpclib;

import android.widget.ArrayAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 02.04.2013
 * Time: 09:57
 */
public class JsonAdapterSortCallback<T extends Comparable<T>> extends JsonCallback<List<T>> {

    private final ArrayAdapter<T> adapter;
    private final Comparator<T> comparator;

    public JsonAdapterSortCallback(ArrayAdapter<T> adapter) {
        this.adapter = adapter;
        comparator = null;
    }

    public JsonAdapterSortCallback(ArrayAdapter<T> adapter, Comparator<T> comparator) {
        this.adapter = adapter;
        this.comparator = comparator;
    }

    public boolean filter(T result) {
        return true;
    }

    @Override
    public void onFinish(List<T> result) {
        adapter.clear();
        if (comparator != null) {
            Collections.sort(result, comparator);
        } else {
            Collections.sort(result);
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
