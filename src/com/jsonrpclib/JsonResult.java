package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public abstract class JsonResult implements Comparable<JsonResult> {

    public Integer id;
    public Object result;
    public Exception error;
    public Object cacheObject;

    @Override
    public int compareTo(JsonResult model) {
        return this.id.compareTo(model.id);
    }
}
