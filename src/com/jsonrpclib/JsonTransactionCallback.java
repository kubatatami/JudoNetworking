package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 14:27
 *
 */
public interface JsonTransactionCallback {

    public void onFinish(Object[] results);
    public void onError(Exception e);

}
