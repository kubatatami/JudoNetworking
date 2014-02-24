package com.github.kubatatami.judonetworking.controllers.raw;


import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.RequestInterface;

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
public class RawRestController extends RawController {

    protected Map<String, Object> customKey = new HashMap<String, Object>();

    public void addCustomKey(String name, Object value) {
        customKey.put(name, value);
    }

    public void removeCustomKey(String name) {
        customKey.remove(name);
    }

    @Override
    public ProtocolController.RequestInfo createRequest(String url, RequestInterface request) throws Exception {
        ProtocolController.RequestInfo requestInfo = new ProtocolController.RequestInfo();
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
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) request.getAdditionalData()).entrySet()) {
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

    @Override
    public void setApiKey(String name, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setApiKey(String key) {
        throw new UnsupportedOperationException();
    }
}
