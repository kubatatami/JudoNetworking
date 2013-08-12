package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.jsonrpclib.JsonRequest;
import com.jsonrpclib.JsonRequestInterface;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public class JsonSimplePostController extends JsonSimpleController {

    public JsonSimplePostController() {
    }

    public JsonSimplePostController(GsonBuilder builder) {
        super(builder);
    }

    @Override
    public RequestInfo createRequest(String url, JsonRequestInterface request, String apiKey) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url=url+request.getName();
        requestInfo.data =JsonController.createRequest(request, apiKey);
        return requestInfo;
    }

}
