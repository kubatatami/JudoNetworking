package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 */
public interface JsonCallbackInterface<T> {

    public void onFinish(T result);

    public void onError(Exception e);

    public void onProgress(int progress);

}
