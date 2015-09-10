package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;

/**
 * Created by Kuba on 10/09/15.
 */
public interface AsyncResultCallback {

    AsyncResult getAsyncResult();
    void setAsyncResult(AsyncResult asyncResult);

}
