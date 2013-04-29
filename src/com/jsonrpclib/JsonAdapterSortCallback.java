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
 *
 */
public class JsonAdapterSortCallback<T extends Comparable<T>> extends JsonCallback<List<T>> {

    private final ArrayAdapter<T> adapter;

    public JsonAdapterSortCallback(ArrayAdapter<T> adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onFinish(List<T> result) {
        adapter.clear();
        Collections.sort(result);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            adapter.setNotifyOnChange(false);
            for (T object : result) {
                adapter.add(object);
            }
            adapter.notifyDataSetChanged();
        } else {
            adapter.addAll(result);
        }
    }
}
