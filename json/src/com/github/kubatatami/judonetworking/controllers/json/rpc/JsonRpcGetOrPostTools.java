package com.github.kubatatami.judonetworking.controllers.json.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.ErrorResult;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.RequestSuccessResult;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Kuba on 11/02/14.
 */
public abstract class JsonRpcGetOrPostTools {


    protected static class JsonGetOrPostResponseModel extends JsonProtocolController.JsonResponseModel {
        public JsonProtocolController.JsonErrorModel error;
    }

    public static RequestResult parseResponse(ObjectMapper mapper, RequestInterface request, InputStream stream) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
            JsonGetOrPostResponseModel response;
            try {
                response = mapper.readValue(inputStreamReader, JsonGetOrPostResponseModel.class);
            } catch (JsonProcessingException ex) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
            }
            if (response.error != null) {
                throw new ProtocolException(response.error.message, response.error.code);
            }
            inputStreamReader.close();
            if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                return new RequestSuccessResult(request.getId(), mapper.readValue(response.result.traverse(), mapper.getTypeFactory().constructType(request.getReturnType())));
            }
            return new RequestSuccessResult(request.getId(), null);
        } catch (Exception e) {
            return new ErrorResult(request.getId(), e);
        }
    }
}
