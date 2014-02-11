package com.judocallbacks;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public class ErrorResult extends RequestResult {


    public ErrorResult(Integer id, Exception error) {
        this.id = id;
        this.error = error;
    }

}
