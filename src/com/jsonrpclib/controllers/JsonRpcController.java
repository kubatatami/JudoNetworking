package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.jsonrpclib.JsonRequestInterface;
import com.jsonrpclib.JsonRpc;

import java.io.IOException;
import java.io.Writer;
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
    public RequestInfo createRequest(String url, JsonRequestInterface request, String apiKey) {
        Object finalArgs;
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url;


        if (request.getParamNames().length > 0 && request.getArgs() != null) {
            int i = 0;
            Map<String, Object> paramObjects = new HashMap<String, Object>();
            for (String param : request.getParamNames()) {
                paramObjects.put(param, request.getArgs()[i]);
                i++;
            }
            finalArgs = paramObjects;
            if (apiKey != null) {
                finalArgs = new Object[]{apiKey, finalArgs};
            }
        } else {
            finalArgs = request.getArgs();
            if (apiKey != null) {
                if (request.getArgs() != null) {
                    Object[] finalArray = new Object[request.getArgs().length + 1];
                    finalArray[0] = apiKey;
                    System.arraycopy(request.getArgs(), 0, finalArray, 1, request.getArgs().length);
                    finalArgs = finalArray;
                } else {
                    finalArgs = new Object[]{apiKey};
                }
            }
        }
        requestInfo.data = createRequestModel(request.getName(), finalArgs, request.getId());
        requestInfo.mimeType = "application/json";
        return requestInfo;
    }

    protected abstract Object createRequestModel(String name, Object params, Integer id);

    @Override
    public boolean isBatchSupported() {
        return false;
    }

    @Override
    public void writeToStream(Writer writer, Object request, int debugFlag) throws IOException {
        if ((debugFlag & JsonRpc.REQUEST_DEBUG) > 0) {
            String req = gson.toJson(request);
            longLog("REQ", req);
            writer.write(req);
        } else {
            gson.toJson(request, writer);
        }
    }


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
