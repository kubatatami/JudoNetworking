package com.github.kubatatami.judonetworking.exceptions;

public class HttpException extends ConnectionException {

    private int code;

    private String body;

    public HttpException(String message, String body, int code) {
        super(message);
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }
}