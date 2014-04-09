package com.github.kubatatami.judonetworking;

/**
 * Created by Kuba on 08/04/14.
 */
public interface EndpointClassic extends EndpointBase{

    public <T> AsyncResult sendAsyncRequest(String url, String name, CallbackInterface<T> callback, Object... args);

    public <T> AsyncResult sendAsyncRequest(String url, String name, RequestOptions requestOptions, CallbackInterface<T> callback, Object... args);

}
