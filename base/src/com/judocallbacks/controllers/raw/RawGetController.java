package com.judocallbacks.controllers.raw;

import com.judocallbacks.RequestInterface;
import com.judocallbacks.controllers.GetOrPostTools;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 21.10.2013
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public class RawGetController extends RawController {


    @Override
    public RequestInfo createRequest(String url, RequestInterface request) {
        ApiKey apiKeyModel = (ApiKey) request.getAdditionalData();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName() + "?" + GetOrPostTools.createRequest(request, apiKeyModel.apiKey, apiKeyModel.apiKeyName);
        return requestInfo;
    }

}
