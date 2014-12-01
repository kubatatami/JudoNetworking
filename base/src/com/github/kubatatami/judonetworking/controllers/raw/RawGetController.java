package com.github.kubatatami.judonetworking.controllers.raw;

import com.github.kubatatami.judonetworking.internals.RequestInterface;
import com.github.kubatatami.judonetworking.controllers.GetOrPostTools;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 21.10.2013
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public class RawGetController extends RawController {


    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws JudoException {
        ApiKey apiKeyModel = (ApiKey) request.getAdditionalData();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName() + "?" + GetOrPostTools.createRequest(request, apiKeyModel.apiKey, apiKeyModel.apiKeyName);
        return requestInfo;
    }

}
