package com.github.kubatatami.judonetworking.batches;

import android.view.View;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kuba on 23/05/14.
 */
public class ViewBatch<T> extends DefaultBatch<T> {

    protected static final Map<Integer, ViewBatch> viewCache = new HashMap<>();
    protected final int viewHash;
    protected AsyncResult asyncResult;

    public ViewBatch(View view) {
        this.viewHash = view.hashCode();
        cancelRequest(viewHash);
        viewCache.put(viewHash, this);
    }

    @Override
    public final void onStart(AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        if (viewCache.get(viewHash) != this) {
            asyncResult.cancel();
        }
        onSafeStart(asyncResult);
    }


    @Override
    public final void onSuccess(Object[] results) {
        super.onSuccess(results);
        if (viewCache.get(viewHash) != this) {
            asyncResult.cancel();
        } else {
            onSafeSuccess(results);
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


    public void onSafeStart(AsyncResult asyncResult) {

    }

    public void onSafeProgress(int progress) {

    }

    public void onSafeSuccess(Object[] result) {

    }

    public void onSafeFinish() {

    }

    public void onSafeError(JudoException e) {

    }

}
