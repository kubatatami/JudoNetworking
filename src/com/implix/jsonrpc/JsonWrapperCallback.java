package com.implix.jsonrpc;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class JsonWrapperCallback<T> implements  JsonCallback<T> {

    ObservableWrapper<T> wrapper;

    public JsonWrapperCallback(ObservableWrapper<T> wrapper)
    {
        this.wrapper=wrapper;
    }

    public void onFinish(T result)
    {
        wrapper.set(result);
    }

    public void onError(Exception e)
    {

    }

}
