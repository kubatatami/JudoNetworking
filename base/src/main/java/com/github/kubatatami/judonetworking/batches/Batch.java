package com.github.kubatatami.judonetworking.batches;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.callbacks.BaseCallback;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public interface Batch<T> extends BaseCallback<Object[]> {

    void onStart(AsyncResult asyncResult);

    void run(final T api);

    void runNonFatal(final T api);

}
