package com.github.kubatatami.judonetworking.controllers.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.HttpException;
import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.RequestException;

import java.io.Serializable;


/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 11:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class JsonProtocolController extends ProtocolController {
    protected ObjectMapper mapper = new ObjectMapper();
    private String apiKey = null;
    private String apiKeyName = null;

    protected JsonProtocolController() {
        mapper = getMapperInstance();
    }

    public static ObjectMapper getMapperInstance(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new EnumAnnotationModule());
        mapper.registerModule(new BooleanModule());
        return mapper;
    }

    public static class JsonResponseModel implements Serializable {
        public JsonNode result;
    }

    public static class JsonErrorModel implements Serializable{
        public String message;
        public int code;
    }

    public void setApiKey(String name, String key) {
        this.apiKeyName = name;
        this.apiKey = key;

    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setApiKey(String key) {
        this.apiKey = key;
    }

    @Override
    public void parseError(int code, String resp) throws Exception {
        if (code == 405) {
            throw new RequestException("Server response: Method Not Allowed. Did you select the correct protocol controller?", new HttpException(resp, code));
        }
    }


    @Override
    public Object getAdditionalRequestData() {
        return new ApiKey(apiKeyName, apiKey);
    }


    protected static class ApiKey {
        public String apiKeyName = null;
        public String apiKey = null;


        public ApiKey(String apiKeyName, String apiKey) {
            this.apiKeyName = apiKeyName;
            this.apiKey = apiKey;
        }
    }

}
