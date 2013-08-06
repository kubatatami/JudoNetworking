package com.jsonrpclib;

import com.google.gson22.JsonElement;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public class JsonResult implements Comparable<JsonResult>{

    public Integer id;
    public Object result;
    public Exception error;
    public Object cacheObject;

    public JsonResult(Object cacheObject) {
        this.cacheObject = cacheObject;
    }

    public JsonResult(Integer id, Object result) {
        this.id = id;
        this.result = result;
    }

    public JsonResult(Integer id, Exception error) {
        this.id = id;
        this.error = error;
    }



    @Override
    public int compareTo(JsonResult model) {
        return this.id.compareTo(model.id);
    }
}
