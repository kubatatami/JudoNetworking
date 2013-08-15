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
public abstract class JsonSimpleController extends JsonProtocolController {

    protected JsonSimpleController() {
    }

    public JsonSimpleController(GsonBuilder builder) {
        super(builder);
    }


    @Override
    public JsonResult parseResponse(JsonRequestInterface request, InputStream stream, int debugFlag, JsonTimeInterface timeStat) {
        try {
            Object res = null;
            if ((debugFlag & JsonRpc.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(stream);
                timeStat.tickReadTime();
                longLog("RES(" + resStr.length() + ")", resStr);
                if (!request.getReturnType().equals(Void.TYPE)) {
                    res = gson.fromJson(resStr, request.getReturnType());
                }
                timeStat.tickParseTime();
            } else {
                JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
                if (!request.getReturnType().equals(Void.TYPE)) {
                    res = gson.fromJson(reader, request.getReturnType());
                }
                timeStat.tickReadTime();
                timeStat.tickParseTime();
            }
            return new JsonSuccessResult(request.getId(), res);
        } catch (Exception e) {
            return new JsonErrorResult(request.getId(), e);
        }
    }

}
