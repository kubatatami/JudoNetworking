package com.judocallbacks.controllers.raw;

import com.judocallbacks.RequestInputStreamEntity;
import com.judocallbacks.RequestInterface;
import com.judocallbacks.controllers.GetOrPostTools;

import java.io.ByteArrayInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 21.10.2013
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public class RawPostController extends RawController {


    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws Exception {
        ApiKey apiKeyModel = (ApiKey) request.getAdditionalData();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName();
        requestInfo.mimeType = "application/x-www-form-urlencoded";
        String reqStr = GetOrPostTools.createRequest(request, apiKeyModel.apiKey, apiKeyModel.apiKeyName);
        byte[] bytes = reqStr.getBytes();
        requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(bytes), bytes.length);
        return requestInfo;
    }

}
