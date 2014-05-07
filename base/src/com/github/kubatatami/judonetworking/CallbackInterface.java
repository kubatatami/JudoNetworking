package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 */
public interface CallbackInterface<T> {

    public void onStart(boolean isCached, AsyncResult asyncResult);

    public void onProgress(int progress);

    public void onSuccess(T result);

    public void onError(JudoException e);

    public void onFinish();

}
