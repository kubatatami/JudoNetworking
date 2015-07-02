package com.github.kubatatami.judonetworking.fragments;

import android.support.v4.app.Fragment;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.batches.DecoratorBatch;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.callbacks.DecoratorCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kuba on 01/07/15.
 */
public class JudoSupportFragment extends Fragment {

    private static final Map<String, Map<Integer, Stateful>> callbacksMap = new HashMap<>();

    private String mWho;

    private String getWho() {
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

    private void removeCallbacks(String who) {
        if (callbacksMap.containsKey(who)) {
            Map<Integer, Stateful> fragmentCallbackMap = callbacksMap.get(who);
            for(Map.Entry<Integer, Stateful> entry : fragmentCallbackMap.entrySet()){
                entry.getValue().setCallback(null);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        removeCallbacks(getWho());
    }

    protected boolean connectCallback(Callback<?> callback) {
        return connectCallback(callback.getClass().hashCode(),callback);
    }

    protected boolean connectCallback(int id, Callback<?> callback) {
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
        return new StatefulCallback<>(this,id, callback);
    }

    protected <T> StatefulBatch<T> generateCallback(Batch<T> batch) {
        return new StatefulBatch<>(this, batch);
    }

    protected <T> StatefulBatch<T> generateCallback(int id, Batch<T> batch) {
        return new StatefulBatch<>(this,id, batch);
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


    interface Stateful<T>{
        void setCallback(T callback);
        void tryCancel();
    }



    public static final class StatefulCallback<T> extends DecoratorCallback<T> implements Stateful<Callback<?>> {

        private AsyncResult asyncResult;
        private final int id;
        private final String who;
        private int progress;
        private boolean consume=false;
        private T data;
        private JudoException exception;

        public StatefulCallback(JudoSupportFragment fragment, Callback<T> callback) {
            this(fragment, callback.getClass().hashCode(), callback);
        }

        public StatefulCallback(JudoSupportFragment fragment, int id, Callback<T> callback) {
            super(callback);
            this.id = id;
            this.who = fragment.getWho();
            addStatefulCallback(who, id, this);
        }

        @Override
        public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
            this.asyncResult = asyncResult;
            consume=false;
            data=null;
            exception=null;
            super.onStart(cacheInfo, asyncResult);
        }

        @Override
        public void onFinish() {
            super.onFinish();
            removeStatefulCallback(who, id);
            consume=true;
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
            }
        }

        @Override
        public void setCallback(Callback<?> callback) {
            this.callback = new WeakReference<>((Callback<T>) callback);
            if (progress > 0) {
                callback.onProgress(progress);
            }
            if(!consume){
                if(data!=null){
                    this.callback.get().onSuccess(data);
                }else if(exception!=null){
                    this.callback.get().onError(exception);
                }
            }
        }

    }




    public static final class StatefulBatch<T> extends DecoratorBatch<T> implements Stateful<Batch<?>>{

        private AsyncResult asyncResult;
        private final int id;
        private final String who;
        private int progress;
        private boolean consume=false;
        private Object[] data;
        private JudoException exception;

        public StatefulBatch(JudoSupportFragment fragment, Batch<T> batch) {
            this(fragment, batch.getClass().hashCode(), batch);
        }

        public StatefulBatch(JudoSupportFragment fragment, int id, Batch<T> batch) {
            super(batch);
            this.id = id;
            this.who = fragment.getWho();
            addStatefulCallback(who, id, this);
        }

        @Override
        public final void onStart(AsyncResult asyncResult) {
            this.asyncResult = asyncResult;
            consume=false;
            data=null;
            exception=null;
            super.onStart(asyncResult);
        }

        @Override
        public void onFinish() {
            super.onFinish();
            removeStatefulCallback(who, id);
            consume=true;
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
            }
        }

        public void setCallback(Batch<?> batch) {
            this.batch = new WeakReference<>((Batch<T>) batch);
            if (progress > 0) {
                batch.onProgress(progress);
            }
            if(!consume){
                if(data!=null){
                    this.batch.get().onSuccess(data);
                }else if(exception!=null){
                    this.batch.get().onError(exception);
                }
            }
        }

    }
}