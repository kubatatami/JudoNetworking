package com.github.kubatatami.judonetworking.exceptions;

/**
 * Created by Kuba on 25/02/14.
 */
public class ProtocolException extends Exception {

    private int code;

    public ProtocolException(String name, Exception e) {
        super(name, e);
    }

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }


}
