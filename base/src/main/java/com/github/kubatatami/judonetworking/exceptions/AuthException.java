package com.github.kubatatami.judonetworking.exceptions;

/**
 * Created by Kuba on 26/02/14.
 */
public class AuthException extends JudoException {

    public AuthException() {
    }

    public AuthException(String detailMessage) {
        super(detailMessage);
    }

    public AuthException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public AuthException(Throwable throwable) {
        super(throwable);
    }
}
