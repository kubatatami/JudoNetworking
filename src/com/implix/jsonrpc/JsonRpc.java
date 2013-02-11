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

    public void setPasswordAuthentication(final String username,final String password);

    public void setJsonVersion(JsonRpcVersion version);

    public void setTimeouts(int connectionTimeout, int methodTimeout, int reconnections);

    public void setCallbackThread(boolean alwaysMainThread);

    public void setDebugFlags(int flags);

    public static final int TIME_DEBUG=1;
    public static final int REQUEST_DEBUG=2;
    public static final int RESPONSE_DEBUG=4;



}
