package org.judonetworking.controllers.json.simple;

import org.judonetworking.ProtocolController;
import org.judonetworking.RequestInterface;
import org.judonetworking.controllers.GetOrPostTools;
import org.judonetworking.controllers.raw.RawController;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public class JsonSimpleGetController extends JsonSimpleController {

    @Override
    public ProtocolController.RequestInfo createRequest(String url, RequestInterface request) {
        ApiKey apiKeyModel = (ApiKey) request.getAdditionalData();
        ProtocolController.RequestInfo requestInfo = new ProtocolController.RequestInfo();
        requestInfo.url = url + request.getName() + "?" + GetOrPostTools.createRequest(request, apiKeyModel.apiKey, apiKeyModel.apiKeyName);
        return requestInfo;
    }

}
