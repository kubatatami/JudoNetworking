package com.judocallbacks.controllers.json.rpc;

import com.google.gson22.GsonBuilder;
import com.judocallbacks.RequestInputStreamEntity;
import com.judocallbacks.RequestInterface;
import com.judocallbacks.controllers.json.JsonProtocolController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

    protected JsonRpcController() {
    }

    protected JsonRpcController(GsonBuilder builder) {
        super(builder);
    }

    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws IOException {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        gson.toJson(createRequestObject(request), writer);
        writer.close();
        requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(stream.toByteArray()), stream.size());
        requestInfo.mimeType = "application/json";
        return requestInfo;
    }


    public Object createRequestObject(RequestInterface request) throws IOException {
        Object finalArgs;
        JsonProtocolController.ApiKey apiKeyModel = (JsonProtocolController.ApiKey) request.getAdditionalData();

        if (request.getParamNames().length > 0 && request.getArgs() != null || apiKeyModel.apiKeyName != null) {
            int i = 0;
            Map<String, Object> paramObjects = new HashMap<String, Object>();
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


    protected class JsonRpcRequestModel {
        private final String method;
        private final Object params;
        private final Integer id;

        public JsonRpcRequestModel(String method, Object params, Integer id) {
            this.method = method;
            this.params = params;
            this.id = id;
        }
    }

    protected class JsonRpcResponseModel extends JsonResponseModel implements Comparable<JsonRpcResponseModel> {
        Integer id;

        @Override
        public int compareTo(JsonRpcResponseModel another) {
            return id.compareTo(another.id);
        }
    }

}
