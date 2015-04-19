package com.github.kubatatami.judonetworking.observers;

import android.content.Context;

import com.github.kubatatami.judonetworking.internals.stats.MethodStat;
import com.github.kubatatami.judonetworking.logs.JudoLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kuba on 18/04/15.
 */
public class ObservablePersistentWrapper<T extends Serializable> extends ObservableWrapper<T> {

    protected Context context;
    protected String persistentKey;

    public ObservablePersistentWrapper(Context context, String persistentKey) {
        this.context = context.getApplicationContext();
        this.persistentKey = persistentKey;
        loadData();
    }

    public ObservablePersistentWrapper(boolean notifyInUiThread, Context context, String persistentKey) {
        super(notifyInUiThread);
        this.context = context.getApplicationContext();
        this.persistentKey = persistentKey;
        loadData();
    }

    public ObservablePersistentWrapper(long updateTime, Context context, String persistentKey) {
        super(updateTime);
        this.context = context.getApplicationContext();
        this.persistentKey = persistentKey;
        loadData();
    }

    public ObservablePersistentWrapper(boolean notifyInUiThread, long updateTime, Context context, String persistentKey) {
        super(notifyInUiThread, updateTime);
        this.context = context.getApplicationContext();
        this.persistentKey = persistentKey;
        loadData();
    }

    public ObservablePersistentWrapper(boolean notifyInUiThread, boolean allowNull, Context context, String persistentKey) {
        super(notifyInUiThread, allowNull);
        this.context = context.getApplicationContext();
        this.persistentKey = persistentKey;
        loadData();
    }

    public ObservablePersistentWrapper(long updateTime, boolean allowNull, Context context, String persistentKey) {
        super(updateTime, allowNull);
        this.context = context.getApplicationContext();
        this.persistentKey = persistentKey;
        loadData();
    }

    public ObservablePersistentWrapper(boolean notifyInUiThread, long updateTime, boolean allowNull, Context context, String persistentKey) {
        super(notifyInUiThread, updateTime, allowNull);
        this.context = context.getApplicationContext();
        this.persistentKey = persistentKey;
        loadData();
    }

    protected File getPersistentFile() {
        File dir = new File(context.getCacheDir() + "/ObservablePersistentWrapper/");
        dir.mkdirs();
        return new File(dir, persistentKey);
    }

    protected void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream fileStream = null;
                ObjectInputStream os = null;
                try {
                    fileStream = new FileInputStream(getPersistentFile());
                    os = new ObjectInputStream(fileStream);
                    PersistentData<T> persistentData = (PersistentData<T>) os.readObject();
                    set(persistentData.object, true, persistentData.dataSetTime);
                } catch (Exception e) {
                    JudoLogger.log(e);
                } finally {
                    try {
                        if (os != null) {
                            os.close();
                        }
                        if (fileStream != null) {
                            fileStream.close();
                        }
                    } catch (IOException ex) {
                        JudoLogger.log(ex);
                    }
                }
            }
        }).start();
    }

    protected void saveData(final T object) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(getPersistentFile()));
                    os.writeObject(new PersistentData<>(updateTime, object));
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    JudoLogger.log(e);
                }
            }
        }).start();
    }

    @Override
    public boolean set(T object, boolean notify) {
        boolean result = super.set(object, notify);
        saveData(object);
        return result;
    }


    protected static class PersistentData<T> implements Serializable {
        public long dataSetTime;
        public T object;

        public PersistentData() {
        }

        public PersistentData(long dataSetTime, T object) {
            this.dataSetTime = dataSetTime;
            this.object = object;
        }
    }
}
