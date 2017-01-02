package com.github.kubatatami.judonetworking.controllers.json.custom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.controllers.ProtocolControllerWrapper;
import com.github.kubatatami.judonetworking.controllers.json.base.JsonProtocolController;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;
import com.github.kubatatami.judonetworking.internals.results.ErrorResult;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.internals.results.RequestSuccessResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
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
    protected ObjectMapper mapper = JsonProtocolController.getMapperInstance();
    protected Class<T> model;

    @SuppressWarnings("unchecked")
    public JsonCustomModelController(ProtocolController baseController, Class<T> model) {
        super(baseController);
        this.model = model;
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


    protected T parseMainModel(InputStreamReader inputStreamReader, Class<T> model) throws IOException {
        return mapper.readValue(inputStreamReader, model);
    }

    @Override
    public RequestResult parseResponse(Request request, InputStream stream, Map<String, List<String>> headers) {
        try {
            T response;
            Object result = null;
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
                response = parseMainModel(inputStreamReader, model);
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


                if ((status != null && !status) || (status == null && (errorMessage != null || errorCode != null))) {
                    throw new ProtocolException(errorMessage != null ? errorMessage : "", errorCode != null ? errorCode : 0);
                }

                if (!request.isVoidResult()) {
                    result = mapper.readValue(data.traverse(), mapper.getTypeFactory().constructType(request.getReturnType()));
                    if (!request.isAllowEmptyResult() && result == null) {
                        throw new ParseException("Empty result.");
                    }
                }
            } catch (JsonProcessingException ex) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
            } catch (IOException ex) {
                throw new ConnectionException(ex);
            }
            return new RequestSuccessResult(request.getId(), result);
        } catch (JudoException e) {
            return new ErrorResult(request.getId(), e);
        }
    }

    protected Boolean getStatus(T responseModel) throws ParseException {
        if (statusField != null) {
            try {
                return (Boolean) statusField.get(responseModel);
            } catch (IllegalAccessException e) {
                throw new ParseException(e);
            }
        } else {
            return null;
        }
    }

    protected String getErrorMessage(T responseModel) throws ParseException {
        if (errorMessageField != null) {
            try {
                return (String) errorMessageField.get(responseModel);
            } catch (IllegalAccessException e) {
                throw new ParseException(e);
            }
        } else {
            return null;
        }
    }

    protected Integer getErrorCode(T responseModel) throws ParseException {
        if (errorCodeField != null) {
            try {
                return (Integer) errorCodeField.get(responseModel);
            } catch (IllegalAccessException e) {
                throw new ParseException(e);
            }
        } else {
            return null;
        }
    }

    protected JsonNode getData(T responseModel) throws ParseException {
        try {
            return (JsonNode) dataField.get(responseModel);
        } catch (IllegalAccessException e) {
            throw new ParseException(e);
        }
    }

    @Override
    public boolean isBatchSupported() {
        return false;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
