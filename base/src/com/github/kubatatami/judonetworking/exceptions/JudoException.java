package com.github.kubatatami.judonetworking.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.01.2013
 * Time: 10:48
 */
public class JudoException extends Exception {

    public JudoException(String message, Exception e) {
        super(message, e);
    }

    public JudoException(String message) {
        super(message);
    }


}
