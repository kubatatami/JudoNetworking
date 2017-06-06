package com.github.kubatatami.judonetworking.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.01.2013
 * Time: 10:48
 */
public class JudoException extends RuntimeException {

    public JudoException() {
    }

    public JudoException(String message, Throwable e) {
        super(message, e);
    }

    public JudoException(Throwable throwable) {
        super(throwable);
    }

    public JudoException(String message) {
        super(message);
    }

    public boolean isConnectionException() {
        return this instanceof ConnectionException && !(this instanceof HttpException);
    }

    public boolean isWrongCredentialsException() {
        return this instanceof HttpException && ((HttpException) this).getCode() == 401;
    }

}
