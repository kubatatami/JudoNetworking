package com.judocallbacks.controllers.json.simple;

import com.google.gson22.GsonBuilder;
import com.judocallbacks.RequestInputStreamEntity;
import com.judocallbacks.RequestInterface;
import com.judocallbacks.controllers.GetOrPostTools;
import com.judocallbacks.controllers.raw.RawController;

import java.io.ByteArrayInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public class JsonSimplePostController extends JsonSimpleController {

    public JsonSimplePostController() {
    }

    public JsonSimplePostController(GsonBuilder builder) {
        super(builder);
    }

    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws Exception {
        RawController.ApiKey apiKeyModel = (RawController.ApiKey) request.getAdditionalData();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url + request.getName();
        requestInfo.mimeType = "application/x-www-form-urlencoded";
        String reqStr = GetOrPostTools.createRequest(request, apiKeyModel.apiKey, apiKeyModel.apiKeyName);
        byte[] bytes = reqStr.getBytes();
        if (bytes.length > 0) {
            requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(bytes), bytes.length);
        }
        return requestInfo;
    }


}
