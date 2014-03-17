package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 29.05.2013
 * Time: 09:46
 * To change this template use File | Settings | File Templates.
 */
public interface ErrorLogger {

    public void onError(JudoException e);

}
