package com.jsonrpclib.observers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.jsonrpclib.JsonNetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ObservableWrapper<T> {
    protected T object = null;
    protected final Handler handler = new Handler(Looper.getMainLooper());
    protected final List<WrapObserver<T>> observers = new ArrayList<WrapObserver<T>>();
    protected ObservableWrapperListener<T> listener = null;
    protected boolean notifyInUiThread = true;
    protected long dataSetTime = 0;
    protected long updateTime = 0;
    protected boolean allowNull = false;
    protected boolean forceUpdateOnNetworkStateChange = false;

    protected boolean checkNetworkState = false;
    protected boolean checkUpdateOnGet = false;

    Timer timer = new Timer();

    protected JsonNetworkUtils.NetworkStateListener networkStateListener = new JsonNetworkUtils.NetworkStateListener() {
        @Override
        public void onNetworkStateChange(boolean networkAvailable) {
            if (networkAvailable) {
                if (forceUpdateOnNetworkStateChange) {
                    forceUpdate();
                } else {
                    checkUpdate();
                }
            }
        }
    };

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

    public ObservableWrapper(boolean notifyInUiThread, boolean allowNull) {
        this.notifyInUiThread = notifyInUiThread;
        this.allowNull = allowNull;
    }

    public ObservableWrapper(long updateTime, boolean allowNull) {
        this.updateTime = updateTime;
        this.allowNull = allowNull;
    }

    public ObservableWrapper(boolean notifyInUiThread, long updateTime, boolean allowNull) {
        this.notifyInUiThread = notifyInUiThread;
        this.updateTime = updateTime;
        this.allowNull = allowNull;
    }

    public void addObserver(WrapObserver<T> observer) {
        addObserver(observer, true);
    }

    public void addObserver(WrapObserver<T> observer, boolean notify) {
        boolean add = true;
        if (listener != null) {
            add = listener.onAddObserver(this, observer);
        }
        if (add) {
            observers.add(observer);
            if (notify && (object != null || allowNull)) {
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

    public void startCheckUpdateOnChangeNetworkState(Context context) {
        startCheckUpdateOnChangeNetworkState(context, false);
    }

    public void startCheckUpdateOnChangeNetworkState(Context context, boolean forceUpdate) {
        if (!checkNetworkState) {
            forceUpdateOnNetworkStateChange = forceUpdate;
            JsonNetworkUtils.addNetworkStateListener(context, networkStateListener);
        }
    }

    public void stopCheckUpdateOnChangeNetworkState(Context context) {
        if (checkNetworkState) {
            JsonNetworkUtils.removeNetworkStateListener(context, networkStateListener);
        }
    }

    public boolean isCheckUpdateOnGet() {
        return checkUpdateOnGet;
    }

    public void setCheckUpdateOnGet(boolean checkUpdateOnGet) {
        this.checkUpdateOnGet = checkUpdateOnGet;
    }


    public T get() {
        if (checkUpdateOnGet) {
            checkUpdate();
        }
        if (listener != null) {
            listener.onGet(this);
        }
        return object;
    }

    public void checkUpdate() {
        if (listener != null && !isDataActual()) {
            listener.onUpdate(this);
        }
    }

    public void forceUpdate() {
        if (listener != null) {
            listener.onUpdate(this);
        }
    }

    public boolean isDataActual() {
        return updateTime == 0 || System.currentTimeMillis() - getDataSetTime() <= updateTime;
    }

    public void set(T object) {
        dataSetTime = System.currentTimeMillis();
        this.object = object;
        notifyObservers();
        if (listener != null) {
            listener.onSet(this, object);
        }
    }

    public void startCheckUpdatePeriodicaly(long period){
        startCheckUpdatePeriodicaly(period,false);
    }

    public void startCheckUpdatePeriodicaly(long period, final boolean forceUpdate) {
        timer.cancel();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (forceUpdate) {
                    forceUpdate();
                } else {
                    checkUpdate();
                }
            }
        }, period, period);
    }

    public void stopCheckUpdatePeriodicaly() {
        timer.cancel();
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