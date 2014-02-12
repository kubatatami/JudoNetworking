package org.judonetworking;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.01.2013
 * Time: 10:48
 */
public class RequestException extends Exception {
    private int code;


    public RequestException(String name, Exception e) {
        super(name, e);
    }

    public RequestException(String message) {
        super(message);
    }

    public RequestException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
