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
    public JsonResult parseResponse(JsonRequest request, InputStream stream, int debugFlag, JsonTimeStat timeStat) {
        try {
            JsonRpcResponseModel1 response = null;
            if ((debugFlag & JsonRpc.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(stream);
                longLog("RES(" + resStr.length() + ")", resStr);
                timeStat.tickReadTime();
                response = gson.fromJson(resStr, JsonRpcResponseModel1.class);
                if (response == null) {
                    throw new JsonException("Can't parse response.");
                } else if (response.error != null) {
                    throw new JsonException(response.error);
                }


            } else {
                JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
                response = gson.fromJson(reader, JsonRpcResponseModel1.class);
                timeStat.tickReadTime();
                if (response.error != null) {
                    throw new JsonException(response.error);
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


    protected class JsonRpcResponseModel1 extends JsonRpcResponseModel {
        String error;
    }

}
