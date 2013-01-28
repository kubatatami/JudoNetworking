package com.implix.jsonrpc;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 14:27
 * To change this template use File | Settings | File Templates.
 */
public interface JsonTransactionCallback {

    public void onFinish(Object[] results);
    public void onError(Exception e);

}
