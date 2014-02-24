package com.github.kubatatami.judonetworking;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 */
public interface CallbackInterface<T> {

    public void onFinish(T result);

    public void onError(Exception e);

    public void onProgress(int progress);

}
