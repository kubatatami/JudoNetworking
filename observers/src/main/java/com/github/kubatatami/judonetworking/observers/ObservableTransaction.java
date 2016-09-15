package com.github.kubatatami.judonetworking.observers;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kuba on 24/05/14.
 */
public class ObservableTransaction {

    protected List<Pair<ObservableWrapper, Object>> wrapperObjectList =
            new ArrayList<>();

    <T> void add(ObservableWrapper<T> observableWrapper, T value) {
        wrapperObjectList.add(new Pair<ObservableWrapper, Object>(observableWrapper, value));
    }

    public int getPreparedActionsCount() {
        return wrapperObjectList.size();
    }

    public void commit() {
        for (int i = wrapperObjectList.size() - 1; i >= 0; i--) {
            Pair<ObservableWrapper, Object> pair = wrapperObjectList.get(i);
            if (!pair.first.set(pair.second, false)) {
                wrapperObjectList.remove(i);
            }
        }
        for (Pair<ObservableWrapper, Object> pair : wrapperObjectList) {
            pair.first.notifyObservers();
        }
    }
}
