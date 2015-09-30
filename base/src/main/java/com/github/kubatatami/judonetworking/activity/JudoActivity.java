package com.github.kubatatami.judonetworking.activity;

import android.app.Activity;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.batches.DecoratorBatch;
import com.github.kubatatami.judonetworking.callbacks.BaseCallback;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.callbacks.DecoratorCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kuba on 01/07/15.
 */
public class JudoActivity extends Activity {

    private static final Map<String, Map<Integer, Stateful>> callbacksMap = new HashMap<>();

    private String getWho() {
        return getTaskId() + "";
    }

    private void removeCallbacks(String who) {
        if (callbacksMap.containsKey(who)) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(who);
            for (Map.Entry<Integer, Stateful> entry : fragmentCallbackMap.entrySet()) {
                entry.getValue().setCallback(null);
            }
        }
    }

    private void removeStatefulCallbacks() {
        if (callbacksMap.containsKey(getWho())) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(getWho());
            for (Map.Entry<Integer, Stateful> entry : fragmentCallbackMap.entrySet()) {
                entry.getValue().tryCancel();
            }
            callbacksMap.remove(getWho());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        removeCallbacks(getWho());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            removeStatefulCallbacks();
        }
    }

    protected boolean connectCallback(BaseCallback<?> callback) {
        return connectCallback(callback.getClass().hashCode(), callback);
    }

    protected boolean connectCallback(int id, BaseCallback<?> callback) {
        if (callbacksMap.containsKey(getWho())) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(getWho());
            if (fragmentCallbackMap.containsKey(id)) {
                fragmentCallbackMap.get(id).setCallback(callback);
                return true;
            }
        }
        return false;
    }

    protected <T> StatefulCallback<T> generateCallback(Callback<T> callback) {
        return new StatefulCallback<>(this, callback);
    }

    protected <T> StatefulCallback<T> generateCallback(int id, Callback<T> callback) {
        return new StatefulCallback<>(this, id, callback);
    }

    protected <T> StatefulBatch<T> generateCallback(Batch<T> batch) {
        return new StatefulBatch<>(this, batch);
    }

    protected <T> StatefulBatch<T> generateCallback(int id, Batch<T> batch) {
        return new StatefulBatch<>(this, id, batch);
    }

    public void cancelRequest(int id) {
        if (callbacksMap.containsKey(getWho())) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(getWho());
            if (fragmentCallbackMap.containsKey(id)) {
                fragmentCallbackMap.get(id).tryCancel();
            }
        }
    }

    static void addStatefulCallback(String who, int id, Stateful statefulCallback) {
        if (!callbacksMap.containsKey(who)) {
            callbacksMap.put(who, new HashMap<Integer, Stateful>());
        }
        Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(who);
        if (fragmentCallbackMap.containsKey(id)) {
            fragmentCallbackMap.get(id).tryCancel();
        }
        fragmentCallbackMap.put(id, statefulCallback);
    }

    static void removeStatefulCallback(String who, int id) {
        if (callbacksMap.containsKey(who)) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(who);
            if (fragmentCallbackMap.containsKey(id)) {
                fragmentCallbackMap.remove(id);
            }
        }
    }


    interface Stateful<T> {

        void setCallback(T callback);

        void tryCancel();
    }


    public static final class StatefulCallback<T> extends DecoratorCallback<T> implements Stateful<Callback<?>> {

        private AsyncResult asyncResult;

        private final int id;

        private final String who;

        private int progress;

        private boolean consume = false;

        private T data;

        private JudoException exception;

        public StatefulCallback(JudoActivity activity, Callback<T> callback) {
            this(activity, callback.getClass().hashCode(), callback);
        }

        public StatefulCallback(JudoActivity activity, int id, Callback<T> callback) {
            super(callback);
            this.id = id;
            this.who = activity.getWho();
            addStatefulCallback(who, id, this);
        }

        @Override
        public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
            this.asyncResult = asyncResult;
            consume = false;
            data = null;
            exception = null;
            super.onStart(cacheInfo, asyncResult);
        }

        @Override
        public void onFinish() {
            super.onFinish();
            if (internalCallback.get() != null) {
                removeStatefulCallback(who, id);
                consume = true;
            }
        }

        @Override
        public void onProgress(int progress) {
            super.onProgress(progress);
            this.progress = progress;
        }

        @Override
        public void tryCancel() {
            if (asyncResult != null) {
                asyncResult.cancel();
                consume = true;
            }
        }

        @Override
        public void setCallback(Callback<?> callback) {
            this.internalCallback = new WeakReference<>((Callback<T>) callback);
            if (callback != null) {
                if (progress > 0) {
                    callback.onProgress(progress);
                }
                if (!consume) {
                    if (data != null) {
                        this.internalCallback.get().onSuccess(data);
                    } else if (exception != null) {
                        this.internalCallback.get().onError(exception);
                    }
                }
            }
        }

    }


    public static final class StatefulBatch<T> extends DecoratorBatch<T> implements Stateful<Batch<?>> {

        private AsyncResult asyncResult;

        private final int id;

        private final String who;

        private int progress;

        private boolean consume = false;

        private Object[] data;

        private JudoException exception;

        public StatefulBatch(JudoActivity activity, Batch<T> batch) {
            this(activity, batch.getClass().hashCode(), batch);
        }

        public StatefulBatch(JudoActivity activity, int id, Batch<T> batch) {
            super(batch);
            this.id = id;
            this.who = activity.getWho();
            addStatefulCallback(who, id, this);
        }

        @Override
        public final void onStart(AsyncResult asyncResult) {
            this.asyncResult = asyncResult;
            consume = false;
            data = null;
            exception = null;
            super.onStart(asyncResult);
        }

        @Override
        public void onFinish() {
            super.onFinish();
            if (batch.get() != null) {
                removeStatefulCallback(who, id);
                consume = true;
            }
        }

        @Override
        public void onProgress(int progress) {
            super.onProgress(progress);
            this.progress = progress;
        }

        @Override
        public void tryCancel() {
            if (asyncResult != null) {
                asyncResult.cancel();
                consume = true;
            }
        }

        public void setCallback(Batch<?> batch) {
            this.batch = new WeakReference<>((Batch<T>) batch);
            if (batch != null) {
                if (progress > 0) {
                    batch.onProgress(progress);
                }
                if (!consume) {
                    if (data != null) {
                        this.batch.get().onSuccess(data);
                    } else if (exception != null) {
                        this.batch.get().onError(exception);
                    }
                }
            }
        }

    }
}