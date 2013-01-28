package com.implix.jsonrpc;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 12:49
 * To change this template use File | Settings | File Templates.
 */
public interface JsonRpc {

    public <T> T getService(Class<T> obj);

    public void startTransaction();
    public void startTransaction(int timeout);
    public Thread endTransaction();
    public Thread endTransaction(JsonTransactionCallback callback);

}
