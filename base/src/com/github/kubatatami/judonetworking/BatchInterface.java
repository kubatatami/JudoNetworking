package com.github.kubatatami.judonetworking;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public interface BatchInterface<T> {

    public void run(final T api);

    public void runNonFatal(final T api);

    public void onFinish(Object[] results);

    public void onError(Exception e);

    public void onProgress(int progress);
}
