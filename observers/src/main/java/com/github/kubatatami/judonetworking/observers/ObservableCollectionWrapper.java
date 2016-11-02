package com.github.kubatatami.judonetworking.observers;

import java.util.Collection;

public class ObservableCollectionWrapper<T extends Collection> extends ObservableWrapper<T> {

    public boolean isEmpty() {
        return !isSet() || get().isEmpty();
    }

    public int size() {
        return isSet() ? get().size() : 0;
    }
}
