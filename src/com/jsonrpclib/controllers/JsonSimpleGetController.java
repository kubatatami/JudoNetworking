package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.google.gson22.stream.JsonReader;
import com.jsonrpclib.JsonRequest;
import com.jsonrpclib.JsonRequestInterface;
import com.jsonrpclib.JsonRpc;
import com.jsonrpclib.JsonTimeStat;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public class JsonSimpleGetController extends JsonSimpleController {

    public JsonSimpleGetController() {
    }

    public JsonSimpleGetController(GsonBuilder builder) {
        super(builder);
    }

    @Override
    public RequestInfo createRequest(String url, JsonRequestInterface request, String apiKey) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url=url+request.getName()+"?"+JsonController.createRequest(request, apiKey);
        return requestInfo;
    }

}
