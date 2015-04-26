package com.github.kubatatami.judonetworking.controllers.json.rpc;

import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.internals.streams.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class JsonRpcController extends JsonProtocolController {


    @Override
    public RequestInfo createRequest(String url, Request request) throws JudoException {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        try {
            mapper.writeValue(writer, createRequestObject(request));
            writer.close();
        } catch (IOException ex) {
            throw new JudoException("Can't create request", ex);
        }
        requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(stream.toByteArray()), stream.size());
        requestInfo.mimeType = "application/json";
        return requestInfo;
    }


    public Object createRequestObject(Request request) throws IOException {
        Object finalArgs;
        ApiKey apiKeyModel = (ApiKey) request.getAdditionalData();
        if (!request.isApiKeyRequired()) {
            apiKeyModel.apiKey = null;
            apiKeyModel.apiKeyName = null;
        }
        if (request.getParamNames().length > 0 && request.getArgs() != null || apiKeyModel.apiKeyName != null) {
            int i = 0;
            Map<String, Object> paramObjects = new HashMap<>();
            for (String param : request.getParamNames()) {
                paramObjects.put(param, request.getArgs()[i]);
                i++;
            }
            finalArgs = paramObjects;
            if (apiKeyModel.apiKey != null) {
                if (apiKeyModel.apiKeyName == null) {
                    finalArgs = new Object[]{apiKeyModel.apiKey, finalArgs};
                } else {
                    paramObjects.put(apiKeyModel.apiKeyName, apiKeyModel.apiKey);
                }

            }
        } else {
            finalArgs = request.getArgs();
            if (apiKeyModel.apiKey != null) {
                if (request.getArgs() != null) {
                    Object[] finalArray = new Object[request.getArgs().length + 1];
                    finalArray[0] = apiKeyModel.apiKey;
                    System.arraycopy(request.getArgs(), 0, finalArray, 1, request.getArgs().length);
                    finalArgs = finalArray;
                } else {
                    finalArgs = new Object[]{apiKeyModel.apiKey};
                }
            }
        }
        return createRequestModel(request.getName(), finalArgs, request.getId());
    }

    protected abstract Object createRequestModel(String name, Object params, Integer id);


    protected static class JsonRpcRequestModel implements Serializable {

        private static final long serialVersionUID = -3197200310517347404L;
        private final String method;
        private final Object params;
        private final Integer id;

        public JsonRpcRequestModel(String method, Object params, Integer id) {
            this.method = method;
            this.params = params;
            this.id = id;
        }
    }

    protected static class JsonRpcResponseModel implements Serializable, Comparable<JsonRpcResponseModel> {

        private static final long serialVersionUID = 6520047711835131472L;
        Integer id;
        public Object result;

        @Override
        public int compareTo(JsonRpcResponseModel another) {
            return id.compareTo(another.id);
        }
    }

}
