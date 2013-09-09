package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.google.gson22.stream.JsonReader;
import com.jsonrpclib.*;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public class JsonGetController extends JsonSimpleGetController {

    public JsonGetController() {
    }

    public JsonGetController(GsonBuilder builder) {
        super(builder);
    }

    @Override
    public JsonResult parseResponse(JsonRequestInterface request, InputStream stream) {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            JsonGetOrPostResponseModel response = gson.fromJson(reader, JsonGetOrPostResponseModel.class);
            if (response.error != null) {
                throw new JsonException(response.error.message, response.error.code);
            }
            reader.close();
            if (!request.getReturnType().equals(Void.TYPE)) {
                return new JsonSuccessResult(request.getId(), gson.fromJson(response.result, request.getReturnType()));
            }
            return new JsonSuccessResult(request.getId(), null);
        } catch (Exception e) {
            return new JsonErrorResult(request.getId(), e);
        }
    }


}
