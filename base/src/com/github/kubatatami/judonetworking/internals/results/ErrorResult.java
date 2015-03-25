package com.github.kubatatami.judonetworking.internals.results;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public class ErrorResult extends RequestResult {


    public ErrorResult(Integer id, JudoException error) {
        this.id = id;
        this.error = error;
    }

}
