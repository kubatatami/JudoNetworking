package com.github.kubatatami.judonetworking;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kuba on 23/05/14.
 */
public class UniqueViewCallback<T> extends Callback<T>{

    protected static final Map<View, UniqueViewCallback> viewCache = new HashMap<View, UniqueViewCallback>();
    protected final View view;
    protected AsyncResult asyncResult;

    public UniqueViewCallback(View view) {
        this.view = view;
        cancelRequest(view);
        viewCache.put(view,this);
    }

    @Override
    public void onStart(boolean isCached, AsyncResult asyncResult) {
        super.onStart(isCached, asyncResult);
        this.asyncResult=asyncResult;
        if(viewCache.get(view)!=this) {
            asyncResult.cancel();
        }
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if(viewCache.containsKey(view) && viewCache.get(view)==this){
            viewCache.remove(view);
        }
    }


    public static void cancelRequest(View view){
        if(viewCache.containsKey(view)) {
            if(viewCache.get(view).asyncResult!=null) {
                viewCache.get(view).asyncResult.cancel();
            }
            viewCache.remove(view);
        }
    }

}
