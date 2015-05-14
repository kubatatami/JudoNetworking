package com.github.kubatatami.judonetworking.observers;

/**
 * Created by Kuba on 13/05/14.
 */
public class ExceptionHandler {

    public static void throwRuntimeException(Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }

}
