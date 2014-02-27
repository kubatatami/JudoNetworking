package com.github.kubatatami.judonetworking.controllers.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.*;
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
public class JsonCustomModelController<T> extends JsonProtocolController {

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

    protected JsonProtocolController baseController;
    protected Class<T> model;

    public JsonCustomModelController(JsonProtocolController baseController) {
        this.baseController = baseController;
        Type[] genericTypes = ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments();
        if (genericTypes.length != 1) {
            throw new RuntimeException("JsonCustomModelController must be generic!");
        }
        model = (Class<T>) genericTypes[0];

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

    @Override
    public ObjectMapper getMapper() {
        return baseController.getMapper();
    }

    @Override
    public void parseError(int code, String resp) throws Exception {
        baseController.parseError(code, resp);
    }

    @Override
    public void setApiKey(String name, String key) {
        baseController.setApiKey(name, key);
    }

    @Override
    public void setApiKey(String key) {
        baseController.setApiKey(key);
    }

    @Override
    public int getAutoBatchTime() {
        return baseController.getAutoBatchTime();
    }

    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws Exception {
        return baseController.createRequest(url, request);
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
        return baseController.isBatchSupported();
    }

    @Override
    public RequestInfo createRequests(String url, List<RequestInterface> requests) throws Exception {
        return baseController.createRequests(url, requests);
    }

    @Override
    public List<RequestResult> parseResponses(List<RequestInterface> requests, InputStream stream, Map<String, List<String>> headers) throws Exception {
        return baseController.parseResponses(requests, stream, headers);
    }

    @Override
    public Object getAdditionalRequestData() {
        return baseController.getAdditionalRequestData();
    }

    @Override
    public TokenCaller getTokenCaller() {
        return baseController.getTokenCaller();
    }

    public JsonProtocolController getBaseController() {
        return baseController;
    }
}
