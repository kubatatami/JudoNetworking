package com.judocallbacks.controllers.json.simple;

import com.google.gson22.Gson;
import com.google.gson22.GsonBuilder;
import com.judocallbacks.RequestInterface;
import com.judocallbacks.RequestResult;
import com.judocallbacks.controllers.raw.RawRestController;

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
public class JsonSimpleRestController extends RawRestController {

    protected Gson gson;

    public JsonSimpleRestController() {
        gson = new Gson();
    }

    public JsonSimpleRestController(GsonBuilder gsonBuilder) {
        this.gson = gsonBuilder.create();
    }

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return JsonSimpleController.parseResponse(gson, request, stream);
    }
}
