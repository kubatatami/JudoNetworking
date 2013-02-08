package com.implix.jsonrpc;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.02.2013
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
public interface WrapObserver<T> {

    public void update(T data);

}
