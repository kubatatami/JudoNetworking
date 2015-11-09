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

    @Override
    public String toString() {
        String msg = getLocalizedMessage();
        String name = getClass().getName();
        String body = getBody();
        if (msg == null && body == null) {
            return name;
        } else if (body == null) {
            return name + ": " + msg;
        } else if (msg == null) {
            return name + ": " + body;
        }
        return name + ": " + msg + " " + body;
    }
}