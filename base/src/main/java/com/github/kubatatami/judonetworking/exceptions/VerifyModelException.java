package com.github.kubatatami.judonetworking.exceptions;

/**
 * Created by Kuba on 25/02/14.
 */
public class VerifyModelException extends JudoException {

    public VerifyModelException() {
    }

    public VerifyModelException(String detailMessage) {
        super(detailMessage);
    }

    public VerifyModelException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public VerifyModelException(Throwable throwable) {
        super(throwable);
    }
}
