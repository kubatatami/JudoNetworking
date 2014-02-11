package com.judocallbacks.controllers.json.simple;

import com.google.gson22.GsonBuilder;
import com.judocallbacks.RequestInterface;
import com.judocallbacks.controllers.GetOrPostTools;
import com.judocallbacks.controllers.raw.RawController;

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
    public RequestInfo createRequest(String url, RequestInterface request) {
        RawController.ApiKey apiKeyModel = (RawController.ApiKey) request.getAdditionalData();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName() + "?" + GetOrPostTools.createRequest(request, apiKeyModel.apiKey, apiKeyModel.apiKeyName);
        return requestInfo;
    }

}
