package com.github.kubatatami.judonetworking.callbacks;

import android.widget.ArrayAdapter;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 02.04.2013
 * Time: 09:57
 */
public class AdapterSortCallback<T extends Comparable<T>> extends DefaultCallback<List<T>> {

    private final WeakReference<ArrayAdapter<T>> adapter;
    private final Comparator<T> comparator;

    public AdapterSortCallback(ArrayAdapter<T> adapter) {
        this.adapter = new WeakReference<>(adapter);
        comparator = null;
    }

    public AdapterSortCallback(ArrayAdapter<T> adapter, Comparator<T> comparator) {
        this.adapter = new WeakReference<>(adapter);
        this.comparator = comparator;
    }

    public boolean filter(T result) {
        return true;
    }

    @Override
    public void onSuccess(List<T> result) {
        ArrayAdapter<T> adapter=this.adapter.get();
        if(adapter!=null) {
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
}
