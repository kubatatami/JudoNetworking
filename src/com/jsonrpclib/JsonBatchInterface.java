package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public interface JsonBatchInterface<T> {

    public void run(final T api);

    public void runNonFatal(final T api);

    public void onFinish(Object[] results);

    public void onError(Exception e);

    public void onProgress(int progress);
}
