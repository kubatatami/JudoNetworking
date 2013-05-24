package com.jsonrpclib.observers;

import com.jsonrpclib.JsonCallback;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 *
 */
public class JsonWrapperCallback<T> extends JsonCallback<T> {

    private final ObservableWrapper<T> wrapper;

    public JsonWrapperCallback(ObservableWrapper<T> wrapper)
    {
        this.wrapper=wrapper;
    }

    @Override
    public void onFinish(T result)
    {
        wrapper.set(result);
    }

}
