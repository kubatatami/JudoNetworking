package com.github.kubatatami.judonetworking.internals.results;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public class RequestSuccessResult extends RequestResult {


    public RequestSuccessResult(Object cacheObject) {
        this.cacheObject = cacheObject;
    }

    public RequestSuccessResult(Integer id, Object result) {
        this.id = id;
        this.result = result;
    }


}
