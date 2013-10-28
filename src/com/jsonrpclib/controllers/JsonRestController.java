package com.jsonrpclib.controllers;

import com.google.gson22.Gson;
import com.google.gson22.GsonBuilder;
import com.google.gson22.stream.JsonReader;
import com.jsonrpclib.JsonRequestInterface;
import com.jsonrpclib.JsonResult;
import com.jsonrpclib.JsonSuccessResult;
import com.jsonrpclib.ProtocolController;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 16.09.2013
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */
public class JsonRestController extends ProtocolController {

    Map<String, Object> customKey = new HashMap<String, Object>();
    Gson gson;

    public JsonRestController() {
        gson = new Gson();
    }

    public JsonRestController(GsonBuilder gsonBuilder) {
        this.gson = gsonBuilder.create();
    }

    public void addCustomKey(String name, Object value) {
        customKey.put(name, value);
    }

    public void removeCustomKey(String name) {
        customKey.remove(name);
    }

    @Override
    public RequestInfo createRequest(String url, JsonRequestInterface request) throws Exception {
        RequestInfo requestInfo = new RequestInfo();
        JsonRest ann = request.getMethod().getAnnotation(JsonRest.class);
        if (ann != null) {
            String result = ann.value();
            if (request.getName() != null) {
                result = result.replaceAll("\\{name\\}", request.getName());
            }
            if (request.getArgs() != null) {
                int i = 0;
                for (Object arg : request.getArgs()) {
                    result = result.replaceAll("\\{" + i + "\\}", arg + "");
                    i++;
                }
            }
            for (Map.Entry<String, Object> entry : customKey.entrySet()) {
                result = result.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue() + "");
            }
            requestInfo.url = url + "/" + result;
        } else {
            requestInfo.url = url + "/" + request.getName();
        }
        return requestInfo;
    }

    @Override
    public JsonResult parseResponse(JsonRequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return new JsonSuccessResult(request.getId(), gson.fromJson(new JsonReader(new InputStreamReader(stream)), request.getReturnType()));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface JsonRest {
        String value() default "";
    }

}
