package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.google.gson22.JsonSyntaxException;
import com.google.gson22.stream.JsonReader;
import com.jsonrpclib.*;

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
public class JsonPostController extends JsonSimplePostController {

    public JsonPostController(GsonBuilder builder) {
        super(builder);
    }

    @Override
    public JsonResult parseResponse(JsonRequestInterface request, InputStream stream, Map<String,List<String>> headers) {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            JsonGetOrPostResponseModel response;
            try {
                response = gson.fromJson(reader, JsonGetOrPostResponseModel.class);
            } catch (JsonSyntaxException ex) {
                throw new JsonException("Wrong server response. Did you select the correct protocol controller?", ex);
            }
            if (response.error != null) {
                throw new JsonException(response.error.message, response.error.code);
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

}
