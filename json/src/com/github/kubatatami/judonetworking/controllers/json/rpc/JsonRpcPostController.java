package com.github.kubatatami.judonetworking.controllers.json.rpc;

import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.controllers.json.simple.JsonSimplePostController;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;

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
public class JsonRpcPostController extends JsonSimplePostController {

    @Override
    public RequestResult parseResponse(Request request, InputStream stream, Map<String, List<String>> headers) {
        return JsonRpcGetOrPostTools.parseResponse(mapper, request, stream);
    }

}
