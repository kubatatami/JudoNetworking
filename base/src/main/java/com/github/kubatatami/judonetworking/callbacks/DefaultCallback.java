package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 */
public class DefaultCallback<T> implements Callback<T>, AsyncResultCallback, CacheInfoCallback {

    private AsyncResult asyncResult;

    private CacheInfo cacheInfo;

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
        this.cacheInfo = cacheInfo;
    }

    @Override
    public void onSuccess(T result) {
    }

    @Override
    public void onError(JudoException e) {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onProgress(int progress) {
    }

    @Override
    public final AsyncResult getAsyncResult() {
        return asyncResult;
    }

    @Override
    public final void setAsyncResult(AsyncResult asyncResult) {
        this.asyncResult = asyncResult;
    }

    @Override
    public CacheInfo getCacheInfo() {
        return cacheInfo;
    }

    @Override
    public void setCacheInfo(CacheInfo cacheInfo) {
        this.cacheInfo = cacheInfo;
    }

}
