package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.jsonrpclib.JsonRequestInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
    public RequestInfo createRequest(String url, JsonRequestInterface request) throws Exception {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName();
        String reqStr=JsonController.createRequest(request, apiKey);
        requestInfo.data = reqStr.getBytes();
        return requestInfo;
    }

}
