package com.github.kubatatami.judonetworking.controllers.json.simple;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 13.08.2013
 * Time: 21:52
 * To change this template use File | Settings | File Templates.
 */
public class JsonSimpleController extends JsonSimpleBaseController {

    ObjectMapper mapper;

    public JsonSimpleController() {
        mapper = getMapperInstance();
    }

    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws JudoException {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.mimeType = "application/json";
        if (url.lastIndexOf("/") != url.length() - 1) {
            url += "/";
        }
        url += request.getName();
        Map<String, Object> req = new HashMap<String, Object>(request.getArgs().length);
        int i = 0;
        for (String paramName : request.getParamNames()) {
            req.put(paramName, request.getArgs()[i]);
            i++;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        try {
            mapper.writeValue(writer, req);
            writer.close();
        } catch (IOException ex) {
            throw new JudoException("Can't create request", ex);
        }

        requestInfo.url = url;
        requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(stream.toByteArray()), stream.size());

        return requestInfo;
    }

}
