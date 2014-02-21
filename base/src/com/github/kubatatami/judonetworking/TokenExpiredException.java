package com.github.kubatatami.judonetworking;

/**
 * Created by Kuba on 21/02/14.
 */
public class TokenExpiredException extends Exception {

    public TokenExpiredException() {
    }

    public TokenExpiredException(String detailMessage) {
        super(detailMessage);
    }

    public TokenExpiredException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TokenExpiredException(Throwable throwable) {
        super(throwable);
    }
}
