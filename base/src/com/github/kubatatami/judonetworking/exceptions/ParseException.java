package com.github.kubatatami.judonetworking.exceptions;

/**
 * Created by Kuba on 25/02/14.
 */
public class ParseException extends JudoException {

    public ParseException() {
    }

    public ParseException(String detailMessage) {
        super(detailMessage);
    }

    public ParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ParseException(Throwable throwable) {
        super(throwable);
    }
}
