package com.github.kubatatami.judonetworking.internals.results;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public abstract class RequestResult implements Comparable<RequestResult> {

    public Integer id;
    public Object result;
    public JudoException error;
    public Object cacheObject;

    public String hash;
    public Long time;

    @Override
    public int compareTo(RequestResult model) {
        return this.id.compareTo(model.id);
    }
}
