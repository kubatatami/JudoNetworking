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
    public JsonResult parseResponse(JsonRequestInterface request, InputStream stream, int debugFlag, JsonTimeInterface timeStat) {
        try {
            JsonGetOrPostResponseModel response = null;
            if ((debugFlag & JsonRpc.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(stream);
                longLog("RES(" + resStr.length() + ")", resStr);
                timeStat.tickReadTime();
                response = gson.fromJson(resStr, JsonGetOrPostResponseModel.class);
                if (response == null) {
                    throw new JsonException("Can't parse response.");
                } else if (response.error != null) {
                    throw new JsonException(response.error.message, response.error.code);
                }


            } else {
                JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
                response = gson.fromJson(reader, JsonGetOrPostResponseModel.class);
                timeStat.tickReadTime();
                if (response.error != null) {
                    throw new JsonException(response.error.message, response.error.code);
                }


                reader.close();

            }

            if (!request.getReturnType().equals(Void.TYPE)) {
                return new JsonResult(request.getId(),gson.fromJson(response.result, request.getReturnType()));
            }
            timeStat.tickParseTime();
            return new JsonResult(request.getId(),null);
        } catch (Exception e) {
            return new JsonResult(request.getId(),e);
        }
    }


}
