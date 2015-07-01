package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.fragments.JudoSupportFragment;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.04.2013
 * Time: 11:40
 */
public final class SupportFragmentCallback<T> extends DecoratorCallback<T> {

    private AsyncResult asyncResult;
    private final int id;
    private final String who;
    private int progress;

    public SupportFragmentCallback(JudoSupportFragment fragment, int id, Callback<T> callback) {
        super(callback);
        this.id=id;
        this.who=fragment.getWho();
        JudoSupportFragment.addCallback(who, id, this);
    }

    @Override
    public final void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        super.onStart(cacheInfo, asyncResult);
    }

    @Override
    public void onFinish() {
        super.onFinish();
        JudoSupportFragment.removeCallback(who, id);
    }

    @Override
    public void onProgress(int progress) {
        super.onProgress(progress);
        this.progress=progress;
    }

    public void tryCancel() {
        if (asyncResult != null) {
            asyncResult.cancel();
        }
    }

    public void setCallback(Callback<?> callback){
        this.callback=new WeakReference<>((Callback<T>) callback);
        if(progress>0){
            callback.onProgress(progress);
        }
    }

}
