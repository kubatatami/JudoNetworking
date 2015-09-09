package com.github.kubatatami.judonetworking.controllers.json.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;
import com.github.kubatatami.judonetworking.controllers.raw.RawRestController;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.internals.streams.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;

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
    public RequestResult parseResponse(Request request, InputStream stream, Map<String, List<String>> headers) {
        return JsonSimpleBaseController.parseResponse(mapper, request, stream);
    }

    @Override
    public RequestInfo createRequest(String url, Request request) throws JudoException {
        ProtocolController.RequestInfo requestInfo = super.createRequest(url, request);
        JsonPost jsonPost = ReflectionCache.getAnnotationInherited(request.getMethod(), JsonPost.class);
        if (jsonPost != null && jsonPost.enabled()) {
            Object finalParams;
            Map<String, Object> params = new HashMap<>();
            int i = 0;
            for (Annotation[] annotations : ReflectionCache.getParameterAnnotations(request.getMethod())) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Post) {
                        params.put(((Post) annotation).value(), request.getArgs()[i]);
                    }
                }
                i++;
            }
            if (jsonPost.singleFlat()) {
                if (params.size() == 1) {
                    finalParams = params.values().iterator().next();
                } else {
                    throw new JudoException("SingleFlat can be enabled only for method with one POST parameter.");
                }
            } else {
                finalParams = params;
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
            if (ann != null && ann.mimeType() != null && !ann.mimeType().equals("")) {
                requestInfo.mimeType = ann.mimeType();
            }
        }
        return requestInfo;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface JsonPost {
        boolean enabled() default true;

        boolean singleFlat() default false;
    }
}
