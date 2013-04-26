package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.01.2013
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class JsonException extends Exception {
    private int code;


    public JsonException(String name, Exception e) {
        super(name,e);
    }

    public JsonException(String message) {
        super(message);
    }

    public JsonException(String message, int code) {
        super(message);
        this.code = code;
    }
    public int getCode() {
        return code;
    }

}
