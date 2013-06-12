package com.jsonrpclib.observers;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class ObservableWrapper<T> {
    private T object = null;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<WrapObserver<T>> observers = new ArrayList<WrapObserver<T>>();
    private ObservableWrapperListener<T> listener = null;
    private boolean notifyInUiThread = true;
    private long dataSetTime = 0;
    private long updateTime = 0;
    private boolean allowNull = false;

    public ObservableWrapper() {

    }

    public ObservableWrapper(boolean notifyInUiThread) {
        this.notifyInUiThread = notifyInUiThread;
    }

    public ObservableWrapper(long updateTime) {
        this.updateTime = updateTime;
    }

    public ObservableWrapper(boolean notifyInUiThread, long updateTime) {
        this.notifyInUiThread = notifyInUiThread;
        this.updateTime = updateTime;
    }

    public ObservableWrapper(boolean notifyInUiThread,boolean allowNull) {
        this.notifyInUiThread = notifyInUiThread;
        this.allowNull=allowNull;
    }

    public ObservableWrapper(long updateTime,boolean allowNull) {
        this.updateTime = updateTime;
        this.allowNull=allowNull;
    }

    public ObservableWrapper(boolean notifyInUiThread, long updateTime,boolean allowNull) {
        this.notifyInUiThread = notifyInUiThread;
        this.updateTime = updateTime;
        this.allowNull=allowNull;
    }




    public void addObserver(WrapObserver<T> observer) {
        boolean add = true;
        if (listener != null) {
            add = listener.onAddObserver(this, observer);
        }
        if (add) {
            observers.add(observer);
            if (object != null) {
                observer.update(get());
            }
        }
    }

    public void deleteObserver(WrapObserver<T> observer) {
        boolean delete = true;
        if (listener != null) {
            delete = listener.onDeleteObserver(this, observer);
        }
        if (delete) {
            observers.remove(observer);
        }
    }

    public T get() {
        if (listener != null) {
            if (updateTime != 0 && System.currentTimeMillis() - getDataSetTime() > updateTime) {
                listener.onUpdate(this);
            }

            listener.onGet(this);
        }
        return object;
    }

    public void set(T object) {
        dataSetTime = System.currentTimeMillis();
        this.object = object;
        notifyObservers();
        if (listener != null) {
            listener.onSet(this, object);
        }
    }

    public long getDataSetTime() {
        return dataSetTime;
    }

    public void notifyObservers() {
        if (object != null || allowNull) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    for (int i = observers.size() - 1; i >= 0; i--) {
                        observers.get(i).update(object);
                    }
                }
            };

            if (Looper.getMainLooper().getThread().equals(Thread.currentThread()) || !notifyInUiThread) {
                runnable.run();
            } else {
                handler.post(runnable);
            }
        }
    }

    public void setListener(ObservableWrapperListener<T> listener) {
        this.listener = listener;
    }

    public int getObserversCount() {
        return observers.size();
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isAllowNull() {
        return allowNull;
    }

    public void setAllowNull(boolean allowNull) {
        this.allowNull = allowNull;
    }
}