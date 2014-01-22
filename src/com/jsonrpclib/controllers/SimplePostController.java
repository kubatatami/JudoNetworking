package com.jsonrpclib.controllers;

import com.jsonrpclib.JsonInputStreamEntity;
import com.jsonrpclib.JsonRequestInterface;

import java.io.ByteArrayInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 21.10.2013
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public class SimplePostController extends SimpleController {


    @Override
    public RequestInfo createRequest(String url, JsonRequestInterface request) throws Exception {
        ApiKey apiKeyModel = (ApiKey) request.getAdditionalData();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName();
        requestInfo.mimeType = "application/x-www-form-urlencoded";
        String reqStr = JsonController.createRequest(request, apiKeyModel.apiKey, apiKeyModel.apiKeyName);
        byte[] bytes = reqStr.getBytes();
        requestInfo.entity = new JsonInputStreamEntity(new ByteArrayInputStream(bytes), bytes.length);
        return requestInfo;
    }

}
