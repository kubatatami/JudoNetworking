package com.implix.jsonrpc;

import java.util.ArrayList;
import java.util.List;

public class ObservableWrapper<T> {
    T object=null;

    List<WrapObserver<T>> observers=new ArrayList<WrapObserver<T>>();

    public void addObserver(WrapObserver<T> observer)
    {
        observers.add(observer);
        observer.update(object);
    }

    public void deleteObserver(WrapObserver<T> observer)
    {
        observers.remove(observer);
    }

    public T get() {
        return object;
    }

    public void set(T object) {
        this.object = object;
        notifyObservers();
    }

    private void notifyObservers()
    {
       for(WrapObserver<T> observer : observers)
       {
           observer.update(object);
       }
    }
}