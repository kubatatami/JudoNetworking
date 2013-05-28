package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.05.2013
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */
public interface JsonProgressObserver {
    public void progressTick();
    public void setMaxProgress(int max);

}
