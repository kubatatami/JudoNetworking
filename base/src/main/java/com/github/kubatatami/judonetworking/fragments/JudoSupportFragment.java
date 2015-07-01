package com.github.kubatatami.judonetworking.fragments;

import android.support.v4.app.Fragment;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.callbacks.DecoratorCallback;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kuba on 01/07/15.
 */
public class JudoSupportFragment extends Fragment {

    private static final Map<String, Map<Integer, StatefulCallback<?>>> callbacksMap = new HashMap<>();

    private String mWho;

    public String getWho() {
        if (mWho == null) {
            try {
                Field whoFiled = Fragment.class.getDeclaredField("mWho");
                whoFiled.setAccessible(true);
                mWho = (String) whoFiled.get(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return mWho;
    }

    protected boolean connectCallback(Callback<?> callback) {
        return connectCallback(callback.getClass().hashCode(),callback);
    }

    protected boolean connectCallback(int id, Callback<?> callback) {
        if (callbacksMap.containsKey(getWho())) {
            Map<Integer, StatefulCallback<?>> fragmentCallbackMap = callbacksMap.get(getWho());
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
        return new StatefulCallback<>(this,id, callback);
    }

    static void addCallback(String who, int id, StatefulCallback<?> statefulCallback) {
        if (!callbacksMap.containsKey(who)) {
            callbacksMap.put(who, new HashMap<Integer, StatefulCallback<?>>());
        }
        Map<Integer, StatefulCallback<?>> fragmentCallbackMap = callbacksMap.get(who);
        if (fragmentCallbackMap.containsKey(id)) {
            fragmentCallbackMap.get(id).tryCancel();
        }
        fragmentCallbackMap.put(id, statefulCallback);
    }

    static void removeCallback(String who, int id) {
        if (callbacksMap.containsKey(who)) {
            Map<Integer, StatefulCallback<?>> fragmentCallbackMap = callbacksMap.get(who);
            if (fragmentCallbackMap.containsKey(id)) {
                fragmentCallbackMap.remove(id);
            }
        }
    }


    /**
     * Created with IntelliJ IDEA.
     * User: jbogacki
     * Date: 23.04.2013
     * Time: 11:40
     */
    public static final class StatefulCallback<T> extends DecoratorCallback<T> {

        private AsyncResult asyncResult;
        private final int id;
        private final String who;
        private int progress;

        public StatefulCallback(JudoSupportFragment fragment, Callback<T> callback) {
            this(fragment, callback.getClass().hashCode(), callback);
        }

        public StatefulCallback(JudoSupportFragment fragment, int id, Callback<T> callback) {
            super(callback);
            this.id = id;
            this.who = fragment.getWho();
            addCallback(who, id, this);
        }

        @Override
        public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
            this.asyncResult = asyncResult;
            super.onStart(cacheInfo, asyncResult);
        }

        @Override
        public void onFinish() {
            super.onFinish();
            removeCallback(who, id);
        }

        @Override
        public void onProgress(int progress) {
            super.onProgress(progress);
            this.progress = progress;
        }

        public void tryCancel() {
            if (asyncResult != null) {
                asyncResult.cancel();
            }
        }

        public void setCallback(Callback<?> callback) {
            this.callback = new WeakReference<>((Callback<T>) callback);
            if (progress > 0) {
                callback.onProgress(progress);
            }
        }

    }
}