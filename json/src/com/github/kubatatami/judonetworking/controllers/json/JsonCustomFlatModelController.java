package com.github.kubatatami.judonetworking.controllers.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuba on 24/02/14.
 */
public class JsonCustomFlatModelController<T> extends JsonCustomModelController<T> {

    public JsonCustomFlatModelController(JsonProtocolController baseController) {
        super(baseController);
    }


    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        try {
            T response;
            InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
            try {
                response = mapper.readValue(inputStreamReader, model);
            } catch (JsonProcessingException ex) {
                throw new RequestException("Wrong server response. Did you select the correct protocol controller?", ex);
            }
            inputStreamReader.close();
            if (response == null) {
                throw new RequestException("Empty response.");
            }
            Boolean success = getStatus(response);
            String message = getErrorMessage(response);
            Integer code = getErrorCode(response);

            if ((success != null && !success) || message != null || code != null) {
                throw new RequestException(message, code);
            }

            inputStreamReader.reset();
            response = mapper.readValue(inputStreamReader, mapper.getTypeFactory().constructType(request.getReturnType()));


            return new RequestSuccessResult(request.getId(), response);
        } catch (Exception e) {
            return new ErrorResult(request.getId(), e);
        }
    }

    @Override
    protected final JsonNode getData(T responseModel) throws Exception {
        throw new UnsupportedOperationException("Flat model don't have a data field.");
    }
}
