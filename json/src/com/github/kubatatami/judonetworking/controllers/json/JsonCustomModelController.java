package com.github.kubatatami.judonetworking.controllers.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.*;
import com.github.kubatatami.judonetworking.controllers.ProtocolControllerWrapper;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;

import java.io.IOException;
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
public class JsonCustomModelController<T> extends ProtocolControllerWrapper {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Status {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ErrorMessage {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ErrorCode {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Data {
    }

    protected Field statusField;
    protected Field errorMessageField;
    protected Field errorCodeField;
    protected Field dataField;
    protected ObjectMapper mapper= JsonProtocolController.getMapperInstance();
    protected Class<T> model;

    @SuppressWarnings("unchecked")
    public JsonCustomModelController(ProtocolController baseController, Class<T> model) {
        super(baseController);
        this.model=model;
        findCustomFields();
    }

    protected void findCustomFields() {
        for (Field field : model.getFields()) {
            if (field.isAnnotationPresent(Status.class)) {
                statusField = field;
            } else if (field.isAnnotationPresent(ErrorMessage.class)) {
                errorMessageField = field;
            }
            if (field.isAnnotationPresent(ErrorCode.class)) {
                errorCodeField = field;
            }
            if (field.isAnnotationPresent(Data.class)) {
                dataField = field;
            }
        }
    }


    protected T parseMainModel(InputStreamReader inputStreamReader, Class<T> model) throws Exception {
        return mapper.readValue(inputStreamReader, model);
    }

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        try {
            T response;
            InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
            try {
                response = parseMainModel(inputStreamReader, model);
            } catch (JsonProcessingException ex) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
            }
            inputStreamReader.close();
            if (response == null) {
                throw new ParseException("Empty response.");
            }
            Boolean status = getStatus(response);
            String errorMessage = getErrorMessage(response);
            Integer errorCode = getErrorCode(response);
            JsonNode data = getData(response);

            if (data == null) {
                throw new ParseException("Data field is required.");
            }

            if ((status != null && !status) || errorMessage != null || errorCode != null) {
                throw new ProtocolException(errorMessage, errorCode);
            }
            Object result = null;
            if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                result = mapper.readValue(data.traverse(), mapper.getTypeFactory().constructType(request.getReturnType()));
                if (!request.isAllowEmptyResult() && result == null) {
                    throw new ParseException("Empty result.");
                }
            }
            return new RequestSuccessResult(request.getId(), result);
        } catch (Exception e) {
            return new ErrorResult(request.getId(), e);
        }
    }

    protected Boolean getStatus(T responseModel) throws Exception {
        if (statusField != null) {
            return (Boolean) statusField.get(responseModel);
        } else {
            return null;
        }
    }

    protected String getErrorMessage(T responseModel) throws Exception {
        if (errorMessageField != null) {
            return (String) errorMessageField.get(responseModel);
        } else {
            return null;
        }
    }

    protected Integer getErrorCode(T responseModel) throws Exception {
        if (errorCodeField != null) {
            return (Integer) errorCodeField.get(responseModel);
        } else {
            return null;
        }
    }

    protected JsonNode getData(T responseModel) throws Exception {
        return (JsonNode) dataField.get(responseModel);
    }

    @Override
    public boolean isBatchSupported() {
        return false;
    }

}
