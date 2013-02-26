package com.implix.jsonrpc;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class ObservableWrapper<T> {
    T object=null;
    Handler handler = new Handler(Looper.getMainLooper());
    List<WrapObserver<T>> observers=new ArrayList<WrapObserver<T>>();

    public void addObserver(WrapObserver<T> observer)
    {
        observers.add(observer);
        if(object!=null)
        {
            observer.update(object);
        }
    }

    public void deleteObserver(WrapObserver<T> observer)
    {
        observers.remove(observer);
    }

    public T get() {
        return object;
    }

    public void set(T object) {
        if(object==null)
        {
            throw new RuntimeException("Do not set null to WrapObserver.");
        }
        this.object = object;
        notifyObservers();
    }

    private void notifyObservers()
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for(int i=observers.size()-1; i>=0;i--)
                {
                    observers.get(i).update(object);
                }
            }
        };

        if(Looper.getMainLooper().getThread().equals(Thread.currentThread()))
        {
            runnable.run();
        }
        else
        {
           handler.post(runnable);
        }

    }
}