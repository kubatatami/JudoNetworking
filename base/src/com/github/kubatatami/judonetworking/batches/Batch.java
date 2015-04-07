package com.github.kubatatami.judonetworking.batches;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.callbacks.BaseCallback;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public interface Batch<T>  extends BaseCallback<Object[]>{

    public void onStart(AsyncResult asyncResult);

    public void run(final T api);

    public void runNonFatal(final T api);

}
