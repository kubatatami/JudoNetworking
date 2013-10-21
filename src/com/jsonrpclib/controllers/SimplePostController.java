package com.jsonrpclib.controllers;

import com.jsonrpclib.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName();
        requestInfo.mimeType="application/x-www-form-urlencoded";
        String reqStr = JsonController.createRequest(request, apiKey, apiKeyName);
        byte[] bytes = reqStr.getBytes();
        requestInfo.entity = new JsonInputStreamEntity(new ByteArrayInputStream(bytes), bytes.length);
        return requestInfo;
    }

}
