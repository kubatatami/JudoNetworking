package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.builder.CallbackBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 */
public interface Callback<T> extends BaseCallback<T> {

    void onStart(CacheInfo cacheInfo, AsyncResult asyncResult);

    class Builder<T> extends CallbackBuilder<T, Callback.Builder<T>> {
        
    }
}
