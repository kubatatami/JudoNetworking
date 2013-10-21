package com.jsonrpclib.controllers;

import com.jsonrpclib.JsonInputStreamEntity;
import com.jsonrpclib.JsonRequestInterface;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

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
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName() + "?" + JsonController.createRequest(request, apiKey, apiKeyName);
        return requestInfo;
    }

}
