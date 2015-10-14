package com.github.kubatatami.judonetworking.callbacks;

import android.view.View;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Kuba on 23/05/14.
 */
public class ViewCallback<T> extends DefaultCallback<T> {

    protected static final Map<Integer, ViewCallback> viewCache = new WeakHashMap<>();

    protected final int viewHash;

    protected AsyncResult asyncResult;

    public ViewCallback(View view) {
        this.viewHash = view.hashCode();
        cancelRequest(viewHash);
        viewCache.put(viewHash, this);
    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        super.onStart(cacheInfo, asyncResult);
        this.asyncResult = asyncResult;
        if (viewCache.get(viewHash) != this) {
            asyncResult.cancel();
        }
        onSafeStart(cacheInfo, asyncResult);
    }

    @Override
    public final void onSuccess(T result) {
        if (viewCache.get(viewHash) != this) {
            asyncResult.cancel();
        } else {
            onSafeSuccess(result);
        }
    }

    @Override
    public final void onError(JudoException e) {
        if (viewCache.get(viewHash) != this) {
            asyncResult.cancel();
        } else {
            onSafeError(e);
        }
    }

    @Override
    public final void onProgress(int progress) {
        if (viewCache.get(viewHash) != this) {
            asyncResult.cancel();
        } else {
            onSafeProgress(progress);
        }
    }

    @Override
    public final void onFinish() {
        super.onFinish();
        if (viewCache.containsKey(viewHash) && viewCache.get(viewHash) == this) {
            viewCache.remove(viewHash);
            onSafeFinish();
        }
    }

    public static void cancelRequest(View view) {
        cancelRequest(view.hashCode());
    }


    public static void cancelRequest(int viewHash) {
        if (viewCache.containsKey(viewHash)) {
            if (viewCache.get(viewHash).asyncResult != null) {
                viewCache.get(viewHash).asyncResult.cancel();
            }
            viewCache.remove(viewHash);
        }
    }


    public void onSafeStart(CacheInfo cacheInfo, AsyncResult asyncResult) {

    }

    public void onSafeProgress(int progress) {

    }

    public void onSafeSuccess(T result) {

    }

    public void onSafeFinish() {

    }

    public void onSafeError(JudoException e) {

    }

}
