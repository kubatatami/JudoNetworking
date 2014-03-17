package com.github.kubatatami.judonetworking.controllers.json.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.kubatatami.judonetworking.ErrorResult;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.RequestSuccessResult;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class JsonRpc1Controller extends JsonRpcController {


    @Override
    protected Object createRequestModel(String name, Object params, Integer id) {
        return new JsonRpcRequestModel(name, params, id);
    }


    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        try {
            JsonRpcResponseModel1 response;
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
                response = mapper.readValue(inputStreamReader, JsonRpcResponseModel1.class);
                inputStreamReader.close();

                if (response == null) {
                    throw new ParseException("Empty server response.");
                }
                if (response.error != null) {
                    throw new ProtocolException(response.error);
                }

                if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                    Object result;

                    result = mapper.readValue(response.result.traverse(), mapper.getTypeFactory().constructType(request.getReturnType()));

                    if (!request.isAllowEmptyResult() && result == null) {
                        throw new ParseException("Empty result.");
                    }
                    return new RequestSuccessResult(request.getId(), result);
                }
            } catch (JsonProcessingException ex) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
            } catch (IOException ex) {
                throw new ConnectionException(ex);
            }
            return new RequestSuccessResult(request.getId(), null);
        } catch (JudoException e) {
            return new ErrorResult(request.getId(), e);
        }
    }


    protected class JsonRpcResponseModel1 extends JsonRpcResponseModel {
        String error;
    }

}
