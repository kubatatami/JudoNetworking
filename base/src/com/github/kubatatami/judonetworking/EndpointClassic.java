package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.internals.EndpointBase;
import com.github.kubatatami.judonetworking.internals.requests.RequestOptions;

/**
 * Created by Kuba on 08/04/14.
 */
public interface EndpointClassic extends EndpointBase {

    public <T> AsyncResult sendAsyncRequest(String url, String name, Callback<T> callback, Object... args);

    public <T> AsyncResult sendAsyncRequest(String url, String name, RequestOptions requestOptions, Callback<T> callback, Object... args);

}
