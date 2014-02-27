package com.github.kubatatami.judonetworking.exceptions;

public class HttpException extends Exception {
    private int code;

    public HttpException(String detailMessage, int code) {
        super(detailMessage);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}