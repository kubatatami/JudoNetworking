package com.github.kubatatami.judonetworking.controllers.raw;

import com.github.kubatatami.judonetworking.internals.streams.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.internals.requests.RequestInterface;
import com.github.kubatatami.judonetworking.controllers.GetOrPostTools;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

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
    public RequestInfo createRequest(String url, RequestInterface request) throws JudoException {
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
