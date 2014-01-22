package com.jsonrpclib.controllers;

import com.google.gson22.Gson;
import com.google.gson22.GsonBuilder;
import com.google.gson22.JsonElement;
import com.jsonrpclib.HttpException;
import com.jsonrpclib.JsonException;
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
    private String apiKey = null;
    private String apiKeyName = null;

    public JsonProtocolController() {
        init(new GsonBuilder());
    }

    public JsonProtocolController(GsonBuilder builder) {
        init(builder);
    }

    private void init(GsonBuilder builder) {
        gson = builder.disableHtmlEscaping().create();
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

    @Override
    public void parseError(int code, String resp) throws Exception {
        if (code == 405) {
            throw new JsonException("Server response: Method Not Allowed. Did you select the correct protocol controller?", new HttpException(resp, code));
        }
    }


    @Override
    public Object getAdditionalRequestData() {
        return new ApiKey(apiKeyName,apiKey);
    }


    protected static class ApiKey
    {
        public String apiKeyName = null;
        public String apiKey = null;


        public ApiKey(String apiKeyName, String apiKey) {
            this.apiKeyName = apiKeyName;
            this.apiKey = apiKey;
        }
    }

}
