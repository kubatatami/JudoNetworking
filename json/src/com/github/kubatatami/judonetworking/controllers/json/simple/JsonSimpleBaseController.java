package com.github.kubatatami.judonetworking.controllers.json.simple;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.ErrorResult;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.RequestSuccessResult;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.ParseException;

import java.io.IOException;
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
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
                if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                    res = mapper.readValue(inputStreamReader, mapper.getTypeFactory().constructType(request.getReturnType()));
                }
                inputStreamReader.close();

            } catch (JsonProcessingException ex) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
            } catch (IOException ex) {
                throw new ConnectionException(ex);
            }
            return new RequestSuccessResult(request.getId(), res);
        } catch (JudoException e) {
            return new ErrorResult(request.getId(), e);
        }
    }

}
