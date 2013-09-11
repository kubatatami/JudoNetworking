package com.jsonrpclib.controllers;

import com.google.gson22.Gson;
import com.google.gson22.GsonBuilder;
import com.google.gson22.JsonElement;
import com.jsonrpclib.ProtocolController;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 11:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class JsonProtocolController extends ProtocolController {
    protected Gson gson;
    protected String apiKey=null;
    protected String apiKeyName=null;

    public JsonProtocolController() {
        init(new GsonBuilder());
    }

    public JsonProtocolController(GsonBuilder builder) {
        init(builder);
    }

    private void init(GsonBuilder builder) {
        gson = builder.create();
    }


    protected class JsonResponseModel {
        JsonElement result;
    }

    protected class JsonGetOrPostResponseModel extends JsonResponseModel {
        JsonErrorModel error;
    }

    protected class JsonErrorModel {
        String message;
        int code;
    }

    public void setApiKey(String name, String key) {
        this.apiKeyName = name;
        this.apiKey = key;
    }

    public void setApiKey(String key) {
        this.apiKey = key;
    }
}
