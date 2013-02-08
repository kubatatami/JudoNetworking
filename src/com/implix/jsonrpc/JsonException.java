package com.implix.jsonrpc;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.01.2013
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class JsonException extends Exception {
    private String message;
    private int code;

    public JsonException(String message) {
        this.message = message;
    }

    public JsonException(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
