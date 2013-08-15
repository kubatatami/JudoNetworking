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
public class JsonRpc2Controller extends JsonRpcController {

    public JsonRpc2Controller() {
    }

    public JsonRpc2Controller(GsonBuilder builder) {
        super(builder);
    }


    @Override
    protected Object createRequestModel(String name, Object params, Integer id) {
        return new JsonRpcRequestModel2(name, params, id);
    }


    @Override
    public JsonResult parseResponse(JsonRequestInterface request, InputStream stream, int debugFlag, JsonTimeInterface timeStat) {
        try {
            JsonRpcResponseModel2 response = null;
            if ((debugFlag & JsonRpc.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(stream);
                longLog("RES(" + resStr.length() + ")", resStr);
                timeStat.tickReadTime();
                response = gson.fromJson(resStr, JsonRpcResponseModel2.class);
                if (response == null) {
                    throw new JsonException("Can't parse response.");
                } else if (response.error != null) {
                    throw new JsonException(response.error.message, response.error.code);
                }


            } else {
                JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
                response = gson.fromJson(reader, JsonRpcResponseModel2.class);
                timeStat.tickReadTime();
                if (response.error != null) {
                    throw new JsonException(response.error.message, response.error.code);
                }


                reader.close();

            }

            if (!request.getReturnType().equals(Void.TYPE)) {
                return new JsonSuccessResult(request.getId(), gson.fromJson(response.result, request.getReturnType()));
            }
            timeStat.tickParseTime();
            return new JsonSuccessResult(request.getId(), null);
        } catch (Exception e) {
            return new JsonErrorResult(e);
        }
    }


    protected class JsonRpcRequestModel2 extends JsonRpcRequestModel {
        final String jsonrpc = "2.0";

        public JsonRpcRequestModel2(String method, Object params, Integer id) {
            super(method, params, id);
        }
    }

    protected class JsonRpcResponseModel2 extends JsonRpcResponseModel {
        JsonErrorModel error;
    }


}
