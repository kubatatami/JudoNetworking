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
public interface Callback<T> {

    public void onStart(CacheInfo cacheInfo, AsyncResult asyncResult);

    public void onProgress(int progress);

    public void onSuccess(T result);

    public void onError(JudoException e);

    public void onFinish();

}
