package com.judocallbacks.controllers.json.simple;

import com.google.gson22.Gson;
import com.google.gson22.GsonBuilder;
import com.judocallbacks.RequestInterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 16.09.2013
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */
public class JsonSimpleRestController extends JsonSimpleController {

    Map<String, Object> customKey = new HashMap<String, Object>();
    Gson gson;

    public JsonSimpleRestController() {
        gson = new Gson();
    }

    public JsonSimpleRestController(GsonBuilder gsonBuilder) {
        this.gson = gsonBuilder.create();
    }

    public void addCustomKey(String name, Object value) {
        customKey.put(name, value);
    }

    public void removeCustomKey(String name) {
        customKey.remove(name);
    }

    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws Exception {
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
            for (Map.Entry<String, Object> entry : ((Map<String, Object>)request.getAdditionalData()).entrySet()) {
                result = result.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue() + "");
            }
            requestInfo.url = url + "/" + result;
        } else {
            requestInfo.url = url + "/" + request.getName();
        }
        return requestInfo;
    }


    @Override
    public Object getAdditionalRequestData() {
        return customKey;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface JsonRest {
        String value() default "";
    }

}
