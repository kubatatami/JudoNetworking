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
public class DefaultCallback<T> implements Callback<T> {

    @Override
    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult) {

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


}
