package com.github.kubatatami.judonetworking.controllers.json.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;
import com.github.kubatatami.judonetworking.controllers.raw.RawRestController;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
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
public class JsonSimpleRestController extends RawRestController {

    protected ObjectMapper mapper;

    public JsonSimpleRestController() {
        mapper = JsonProtocolController.getMapperInstance();
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return JsonSimpleBaseController.parseResponse(mapper, request, stream);
    }

    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws JudoException {
        if(request.getMethod().isAnnotationPresent(JsonPost.class)) {
            ProtocolController.RequestInfo requestInfo = super.createRequest(url, request);
            Map<String, Object> params = new HashMap<String, Object>();
            int i=0;
            for(Annotation[] annotations : request.getMethod().getParameterAnnotations()){
                for(Annotation annotation : annotations){
                    if(annotation instanceof Post){
                        Object arg = request.getArgs()[i];
                        params.put(((Post)annotation).value(),arg);
                    }
                }
                i++;
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            try{
                mapper.writeValue(writer, params);
                writer.close();
            }catch (IOException ex){
                throw new JudoException("Can't create request",ex);
            }

            requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(stream.toByteArray()), stream.size());
            requestInfo.mimeType = "application/json";
            return requestInfo;
        }else {
            return super.createRequest(url, request);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface JsonPost {}
}
