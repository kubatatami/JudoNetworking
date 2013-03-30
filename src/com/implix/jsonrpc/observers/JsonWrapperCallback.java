package com.implix.jsonrpc.observers;

import com.implix.jsonrpc.JsonCallback;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class JsonWrapperCallback<T> extends JsonCallback<T> {

    ObservableWrapper<T> wrapper;

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
