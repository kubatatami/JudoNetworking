package com.jsonrpclib.controllers;

import com.jsonrpclib.JsonRequestInterface;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 21.10.2013
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public class SimpleGetController extends SimpleController {


    @Override
    public RequestInfo createRequest(String url, JsonRequestInterface request) {
        ApiKey apiKeyModel = (ApiKey) request.getAdditionalData();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName() + "?" + JsonController.createRequest(request, apiKeyModel.apiKey, apiKeyModel.apiKeyName);
        return requestInfo;
    }

}
