package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public class JsonErrorResult extends JsonResult {

    public JsonErrorResult(Exception error) {
        this.error = error;
    }

    public JsonErrorResult(Integer id, Exception error) {
        this.id = id;
        this.error = error;
    }

}
