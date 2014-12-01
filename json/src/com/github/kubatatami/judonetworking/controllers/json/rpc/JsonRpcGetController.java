package com.github.kubatatami.judonetworking.controllers.json.rpc;

import com.github.kubatatami.judonetworking.internals.RequestInterface;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.controllers.json.simple.JsonSimpleGetController;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public class JsonRpcGetController extends JsonSimpleGetController {


    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return JsonRpcGetOrPostTools.parseResponse(mapper, request, stream);
    }


}
