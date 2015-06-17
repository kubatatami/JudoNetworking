package com.github.kubatatami.judonetworking.observers;

import android.content.Context;

import com.github.kubatatami.judonetworking.logs.JudoLogger;
import com.github.kubatatami.judonetworking.utils.MoveToHeadThreadPoolExecutor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.concurrent.Semaphore;

/**
 * Created by Kuba on 18/04/15.
 */
public abstract class ObservablePersistentWrapper<T> extends ObservableWrapper<T> {

    protected static Level defaultLevel = Level.DATA;

    protected static MoveToHeadThreadPoolExecutor loaderExecutor = new MoveToHeadThreadPoolExecutor(0, 1);
    protected static MoveToHeadThreadPoolExecutor deserializatorExecutor = new MoveToHeadThreadPoolExecutor();

    protected class DeserializationRunnable implements Runnable {

        protected long readTimeStart;
        protected long readTimeStop;
        protected byte[] data;

        public DeserializationRunnable(long readTimeStart, long readTimeStop, byte[] data) {
            this.data = data;
            this.readTimeStart = readTimeStart;
            this.readTimeStop = readTimeStop;
        }

        @Override
        public void run() {
            try {
                long deserializationTimeStart = System.currentTimeMillis();
                PersistentData<T> persistentData = loadObject(data);
                set(persistentData.object, true, persistentData.dataSetTime);
                JudoLogger.log("ObservablePersistentWrapper " + persistentKey
                        + " readTime: " + (readTimeStop - readTimeStart) +
                        "ms waitToDeserializationTime: " + (deserializationTimeStart - readTimeStop) +
                        "ms deserializationTime: " + (System.currentTimeMillis() - deserializationTimeStart) + "ms")
                ;
            } catch (Exception e) {
                JudoLogger.log(e);
            } finally {
                semaphore.release();
            }
        }
    }


    protected Context context;
    protected String persistentKey;
    protected Level level = defaultLevel;
    protected boolean loaded;
    protected boolean waiting;
    protected Semaphore semaphore = new Semaphore(1, true);
    protected Runnable loadingRunnable = new Runnable() {
        @Override
        public void run() {
            loadDataSync();
        }
    };

    protected Runnable deserializationRunnable;

    public ObservablePersistentWrapper(Context context, String persistentKey) {
        this.context = context;
        this.persistentKey = persistentKey;
        loadDataAsync();
    }

    public ObservablePersistentWrapper(Context context, String persistentKey, Level level) {
        this.context = context;
        this.persistentKey = persistentKey;
        this.level = level;
        loadDataAsync();
    }

    protected static File getPersistentDir(Level level, Context context) {
        File dir = new File((level.equals(Level.CACHE) ? context.getCacheDir() : context.getFilesDir()) + "/ObservablePersistentWrapper/");
        dir.mkdirs();
        return dir;
    }

    protected File getPersistentFile() {
        return new File(getPersistentDir(level, context), persistentKey);
    }

    @Override
    public synchronized T get() {
        try {
            long time = System.currentTimeMillis();
            if (loaderExecutor.moveToHead(loadingRunnable)) {
                JudoLogger.log("ObservablePersistentWrapper " + persistentKey + " move to loader queue head");
            } else {
                if (deserializatorExecutor.moveToHead(deserializationRunnable)) {
                    JudoLogger.log("ObservablePersistentWrapper " + persistentKey + " move to deserializator queue head");
                }
            }
            waiting = true;
            semaphore.acquire();
            T result = super.get();
            semaphore.release();
            waiting = false;
            time = System.currentTimeMillis() - time;
            if (time > 1) {
                JudoLogger.log("ObservablePersistentWrapper " + persistentKey + " waiting: " + time + "ms");
            }
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract PersistentData<T> loadObject(byte[] array) throws Exception;

    protected abstract void saveObject(OutputStream fileStream, PersistentData<T> data) throws Exception;

    protected void loadDataSync() {
        File file = getPersistentFile();
        if (file.exists()) {
            try {
                final long readTimeStart = System.currentTimeMillis();
                RandomAccessFile ra = new RandomAccessFile(file, "rw");
                final byte[] b = new byte[(int) file.length()];
                ra.read(b);
                ra.close();
                final long readTimeStop = System.currentTimeMillis();
                deserializationRunnable = new DeserializationRunnable(readTimeStart, readTimeStop, b);
                deserializatorExecutor.execute(deserializationRunnable);
                if (waiting) {
                    if (deserializatorExecutor.moveToHead(deserializationRunnable)) {
                        JudoLogger.log("ObservablePersistentWrapper " + persistentKey + " move to deserializator queue head");
                    }
                }
            } catch (Exception e) {
                JudoLogger.log(e);
                semaphore.release();
            }
        } else {
            semaphore.release();
        }
        loaded = true;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    protected void loadDataAsync() {
        try {
            semaphore.acquire();
            loaderExecutor.execute(loadingRunnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void saveData(final T object) {
        loaderExecutor.execute(new Runnable() {
            @Override
            public void run() {
                OutputStream fileStream = null;
                try {
                    fileStream = new BufferedOutputStream(new FileOutputStream(getPersistentFile()));
                    saveObject(fileStream, new PersistentData<>(dataSetTime, object));
                } catch (Exception e) {
                    JudoLogger.log(e);
                } finally {
                    try {
                        if (fileStream != null) {
                            fileStream.close();
                        }
                    } catch (IOException ex) {
                        JudoLogger.log(ex);
                    }
                }
            }
        });
    }

    @Override
    public boolean set(T object, boolean notify) {
        boolean result = super.set(object, notify);
        saveData(object);
        return result;
    }

    public static void removeAllDataSync(Context context) {
        removeAllDataSync(defaultLevel, context);
    }

    public static void removeAllDataAsync(final Context context) {
        removeAllDataAsync(defaultLevel, context);
    }

    public static void removeAllDataSync(Level level, Context context) {
        for (File file : getPersistentDir(level, context).listFiles()) file.delete();
    }

    public static void removeAllDataAsync(final Level level, final Context context) {
        loaderExecutor.execute(new Runnable() {
            @Override
            public void run() {
                removeAllDataSync(level, context);
            }
        });
    }

    protected enum Level {
        CACHE, DATA
    }

    protected static class PersistentData<T> implements Serializable {

        private static final long serialVersionUID = -6391279026652710936L;

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
