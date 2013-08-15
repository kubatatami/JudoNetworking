package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public class JsonSuccessResult extends JsonResult {


    public JsonSuccessResult(Object cacheObject) {
        this.cacheObject = cacheObject;
    }

    public JsonSuccessResult(Integer id, Object result) {
        this.id = id;
        this.result = result;
    }


}
