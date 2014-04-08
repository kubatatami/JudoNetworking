package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public interface BatchInterface<T> {

    public void onStart();

    public void run(final T api);

    public void runNonFatal(final T api);

    public void onProgress(int progress);

    public void onError(JudoException e);

    public void onSuccess(Object[] results);

    public void onFinish();

}
