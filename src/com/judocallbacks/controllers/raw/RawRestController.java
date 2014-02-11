package com.judocallbacks.controllers.raw;

import com.judocallbacks.RequestInterface;
import com.judocallbacks.RequestResult;
import com.judocallbacks.controllers.json.simple.JsonSimpleRestController;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 16.09.2013
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */
public class RawRestController extends JsonSimpleRestController {

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return RawController.parseResponse(request, stream);
    }

}
