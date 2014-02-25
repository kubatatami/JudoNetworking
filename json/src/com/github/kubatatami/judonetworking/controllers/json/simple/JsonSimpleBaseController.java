package com.github.kubatatami.judonetworking.controllers.json.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.ErrorResult;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.RequestSuccessResult;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class JsonSimpleBaseController extends JsonProtocolController {

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return parseResponse(mapper, request, stream);
    }


    public static RequestResult parseResponse(ObjectMapper mapper, RequestInterface request, InputStream stream) {
        try {
            Object res = null;
            InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
            if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                res = mapper.readValue(inputStreamReader, mapper.getTypeFactory().constructType(request.getReturnType()));
            }
            inputStreamReader.close();
            return new RequestSuccessResult(request.getId(), res);
        } catch (Exception e) {
            return new ErrorResult(request.getId(), e);
        }
    }

}
