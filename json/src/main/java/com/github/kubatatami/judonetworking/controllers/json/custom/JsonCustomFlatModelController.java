package com.github.kubatatami.judonetworking.controllers.json.custom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;
import com.github.kubatatami.judonetworking.internals.results.ErrorResult;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.internals.results.RequestSuccessResult;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuba on 24/02/14.
 */
public class JsonCustomFlatModelController<T> extends JsonCustomModelController<T> {

    public JsonCustomFlatModelController(ProtocolController baseController, Class<T> model) {
        super(baseController, model);
    }

    protected T parseMainModel(String inputString, Class<T> model) throws IOException {
        return mapper.readValue(inputString, model);
    }

    protected Object parseFinalModel(String inputString, Type type) throws IOException {
        return mapper.readValue(inputString, mapper.getTypeFactory().constructType(type));
    }

    @Override
    public RequestResult parseResponse(Request request, InputStream stream, Map<String, List<String>> headers) {
        try {
            T response;
            String responseString = convertStreamToString(stream);
            try {
                response = parseMainModel(responseString, model);
            } catch (JsonProcessingException ex) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
            } catch (IOException ex) {
                throw new ConnectionException(ex);
            }
            if (response == null) {
                throw new ParseException("Empty response.");
            }
            Boolean success = getStatus(response);
            String message = getErrorMessage(response);
            Integer code = getErrorCode(response);

            if ((success != null && !success) || message != null || code != null) {
                throw new ProtocolException(message != null ? message : "", code != null ? code : 0);
            }
            try {
                return new RequestSuccessResult(request.getId(), parseFinalModel(responseString, request.getReturnType()));
            } catch (JsonProcessingException ex) {
                throw new ParseException(ex);
            } catch (IOException ex) {
                throw new ConnectionException(ex);
            }
        } catch (JudoException e) {
            return new ErrorResult(request.getId(), e);
        }
    }

    @Override
    protected final JsonNode getData(T responseModel) throws JudoException {
        throw new UnsupportedOperationException("Flat model don't have a data field.");
    }
}
