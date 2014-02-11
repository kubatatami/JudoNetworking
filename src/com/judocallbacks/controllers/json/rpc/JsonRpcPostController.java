package com.judocallbacks.controllers.json.rpc;

import com.google.gson22.GsonBuilder;
import com.google.gson22.JsonSyntaxException;
import com.google.gson22.stream.JsonReader;
import com.judocallbacks.*;
import com.judocallbacks.controllers.json.simple.JsonSimplePostController;

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
public class JsonRpcPostController extends JsonSimplePostController {

    public JsonRpcPostController(GsonBuilder builder) {
        super(builder);
    }

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            JsonRpcGetController.JsonGetOrPostResponseModel response;
            try {
                response = gson.fromJson(reader, JsonRpcGetController.JsonGetOrPostResponseModel.class);
            } catch (JsonSyntaxException ex) {
                throw new RequestException("Wrong server response. Did you select the correct protocol controller?", ex);
            }
            if (response.error != null) {
                throw new RequestException(response.error.message, response.error.code);
            }
            reader.close();
            if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                return new RequestSuccessResult(request.getId(), gson.fromJson(response.result, request.getReturnType()));
            }
            return new RequestSuccessResult(request.getId(), null);
        } catch (Exception e) {
            return new ErrorResult(request.getId(), e);
        }
    }

}
