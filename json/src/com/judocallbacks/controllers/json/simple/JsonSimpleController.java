package com.judocallbacks.controllers.json.simple;

import com.google.gson22.Gson;
import com.google.gson22.GsonBuilder;
import com.google.gson22.stream.JsonReader;
import com.judocallbacks.controllers.json.JsonProtocolController;
import com.judocallbacks.ErrorResult;
import com.judocallbacks.RequestInterface;
import com.judocallbacks.RequestResult;
import com.judocallbacks.RequestSuccessResult;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class JsonSimpleController extends JsonProtocolController {

    protected JsonSimpleController() {
    }

    public JsonSimpleController(GsonBuilder builder) {
        super(builder);
    }


    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return parseResponse(gson, request, stream);
    }


    public static RequestResult parseResponse(Gson gson, RequestInterface request, InputStream stream) {
        try {
            Object res = null;
            JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                res = gson.fromJson(reader, request.getReturnType());
            }
            return new RequestSuccessResult(request.getId(), res);
        } catch (Exception e) {
            return new ErrorResult(request.getId(), e);
        }
    }

}
