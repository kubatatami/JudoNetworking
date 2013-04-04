package com.implix.jsonrpc;

import android.os.Build;
import android.widget.ArrayAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 02.04.2013
 * Time: 09:57
 * To change this template use File | Settings | File Templates.
 */
public class JsonAdapterSortCallback<T extends Comparable<T>> extends JsonCallback<List<T>> {

    ArrayAdapter<T> adapter;

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
