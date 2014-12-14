package com.github.kubatatami.judonetworking.controllers.json.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.internals.results.ErrorResult;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.internals.results.RequestSuccessResult;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Kuba on 11/02/14.
 */
public abstract class JsonRpcGetOrPostTools {


    protected static class JsonGetOrPostResponseModel extends JsonProtocolController.JsonResponseModel {
        public JsonProtocolController.JsonErrorModel error;
    }

    public static RequestResult parseResponse(ObjectMapper mapper, Request request, InputStream stream) {
        try {

            JsonGetOrPostResponseModel response;
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
                response = mapper.readValue(inputStreamReader, JsonGetOrPostResponseModel.class);
                inputStreamReader.close();

                if (response.error != null) {
                    throw new ProtocolException(response.error.message, response.error.code);
                }

                if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                    return new RequestSuccessResult(request.getId(), mapper.readValue(response.result.traverse(), mapper.getTypeFactory().constructType(request.getReturnType())));
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
}
