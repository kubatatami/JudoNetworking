package com.github.kubatatami.judonetworking;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 14:27
 */
public interface TransactionCallback {

    public void onFinish(Object[] results);

    public void onError(Exception e);

}
