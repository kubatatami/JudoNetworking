package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.google.gson22.stream.JsonReader;
import com.jsonrpclib.*;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class JsonRpc1Controller extends JsonRpcController {

    public JsonRpc1Controller() {
    }

    public JsonRpc1Controller(GsonBuilder builder) {
        super(builder);
    }


    @Override
    protected Object createRequestModel(String name, Object params, Integer id) {
        return new JsonRpcRequestModel(name, params, id);
    }


    @Override
    public JsonResult parseResponse(JsonRequestInterface request, InputStream stream) {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            JsonRpcResponseModel1 response = gson.fromJson(reader, JsonRpcResponseModel1.class);
            if (response.error != null) {
                throw new JsonException(response.error);
            }


            reader.close();
            if (!request.getReturnType().equals(Void.class)) {
                return new JsonSuccessResult(request.getId(), gson.fromJson(response.result, request.getReturnType()));
            }
            return new JsonSuccessResult(request.getId(), null);
        } catch (Exception e) {
            return new JsonErrorResult(request.getId(), e);
        }
    }


    protected class JsonRpcResponseModel1 extends JsonRpcResponseModel {
        String error;
    }

}
