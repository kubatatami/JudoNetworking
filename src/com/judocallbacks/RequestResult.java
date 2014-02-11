package com.judocallbacks;

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
    public Exception error;
    public Object cacheObject;

    public String hash;
    public Long time;

    @Override
    public int compareTo(RequestResult model) {
        return this.id.compareTo(model.id);
    }
}
