package org.judonetworking.controllers.json.simple;

import org.judonetworking.RequestInputStreamEntity;
import org.judonetworking.RequestInterface;
import org.judonetworking.controllers.GetOrPostTools;
import org.judonetworking.controllers.raw.RawController;

import java.io.ByteArrayInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public class JsonSimplePostController extends JsonSimpleController {

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
