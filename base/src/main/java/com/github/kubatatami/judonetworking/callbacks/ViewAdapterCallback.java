package com.github.kubatatami.judonetworking.callbacks;

import android.view.View;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Kuba on 23/05/14.
 */
public class ViewAdapterCallback<Z, T> extends DefaultCallback<T> {

    protected static final Map<View, ViewAdapterCallback> viewCache = new WeakHashMap<>();

    protected final View view;

    public ViewAdapterCallback(View view) {
        this.view = view;
    }

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        super.onStart(cacheInfo, asyncResult);
        ViewAdapterCallback oldCallback = viewCache.get(view);
        if (oldCallback != null && oldCallback != this) {
            oldCallback.getAsyncResult().cancel();
        }
        viewCache.put(view, this);
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (viewCache.containsKey(view) && viewCache.get(view) == this) {
            viewCache.remove(view);
        }
    }


    public static void cancelRequest(View view) {
        if (viewCache.containsKey(view)) {
            if (viewCache.get(view).getAsyncResult() != null) {
                viewCache.get(view).getAsyncResult().cancel();
            }
            viewCache.remove(view);
        }
    }

}
