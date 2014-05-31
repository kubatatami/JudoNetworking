package com.github.kubatatami.judonetworking.controllers.json.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.ReflectionCache;
import com.github.kubatatami.judonetworking.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;
import com.github.kubatatami.judonetworking.controllers.raw.RawRestController;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

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
        JsonPost jsonPost = ReflectionCache.getAnnotationInherited(request.getMethod(), JsonPost.class);
        if (jsonPost!=null && jsonPost.enabled()) {
            ProtocolController.RequestInfo requestInfo = super.createRequest(url, request);
            Object finalParams;
            if(jsonPost.singleFlat()){
                if(request.getArgs().length==1){
                    finalParams=request.getArgs()[0];
                }else{
                    throw new JudoException("SingleFlat can be enabled only for method with one parameter.");
                }
            }else {
                Map<String, Object> params = new HashMap<String, Object>();
                int i = 0;
                for (String name : request.getParamNames()) {
                    params.put(name, request.getArgs()[i]);
                    i++;
                }
                finalParams=params;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            try {
                mapper.writeValue(writer, finalParams);
                writer.close();
            } catch (IOException ex) {
                throw new JudoException("Can't create request", ex);
            }

            requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(stream.toByteArray()), stream.size());
            requestInfo.mimeType = "application/json";

            Rest ann = ReflectionCache.getAnnotationInherited(request.getMethod(), Rest.class);
            if (ann.mimeType() != null) {
                requestInfo.mimeType = ann.mimeType();
            }

            return requestInfo;
        } else {
            return super.createRequest(url, request);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface JsonPost {
        boolean enabled() default true;
        boolean singleFlat() default false;
    }
}
