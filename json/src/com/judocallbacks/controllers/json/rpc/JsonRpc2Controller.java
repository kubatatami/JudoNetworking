package com.judocallbacks.controllers.json.rpc;

import com.google.gson22.GsonBuilder;
import com.google.gson22.JsonSyntaxException;
import com.google.gson22.reflect.TypeToken;
import com.google.gson22.stream.JsonReader;
import com.judocallbacks.controllers.json.JsonProtocolController;
import com.judocallbacks.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class JsonRpc2Controller extends JsonRpcController {

    boolean batchEnabled = false;

    public JsonRpc2Controller() {
    }

    public JsonRpc2Controller(boolean batchEnabled) {
        this.batchEnabled = batchEnabled;
    }

    public JsonRpc2Controller(GsonBuilder builder) {
        super(builder);
    }

    public JsonRpc2Controller(GsonBuilder builder, boolean batchEnabled) {
        super(builder);
        this.batchEnabled = batchEnabled;
    }

    @Override
    protected Object createRequestModel(String name, Object params, Integer id) {
        return new JsonRpcRequestModel2(name, params, id);
    }

    @Override
    public boolean isBatchSupported() {
        return batchEnabled;
    }


    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            JsonRpcResponseModel2 response;
            try {
                response = gson.fromJson(reader, JsonRpcResponseModel2.class);
            } catch (JsonSyntaxException ex) {
                throw new RequestException("Wrong server response. Did you select the correct protocol controller?", ex);
            }

            if (response == null) {
                throw new RequestException("Empty response.");
            }

            if (response.jsonrpc == null) {
                throw new RequestException("Wrong server response. Did you select the correct protocol controller? Maybe your server doesn't support json-com.judocallbacks.controllers.json.rpc 2.0? Try JsonRpc1Controller.");
            }

            if (response.error != null) {
                throw new RequestException(response.error.message, response.error.code);
            }
            reader.close();
            Object result = null;
            if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                result = gson.fromJson(response.result, request.getReturnType());
                if (!request.isAllowEmptyResult() && result == null) {
                    throw new RequestException("Empty result.");
                }
            }
            return new RequestSuccessResult(request.getId(), result);
        } catch (Exception e) {
            return new ErrorResult(request.getId(), e);
        }
    }

    @Override
    public ProtocolController.RequestInfo createRequest(String url, List<RequestInterface> requests) throws Exception {
        int i = 0;
        ProtocolController.RequestInfo requestInfo = new ProtocolController.RequestInfo();
        requestInfo.url = url;
        Object[] requestsJson = new Object[requests.size()];
        for (RequestInterface request : requests) {
            requestsJson[i] = createRequestObject(request);
            i++;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        gson.toJson(requestsJson, writer);
        writer.close();
        requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(stream.toByteArray()), stream.size());
        requestInfo.mimeType = "application/json";
        return requestInfo;
    }

    @Override
    public List<RequestResult> parseResponses(List<RequestInterface> requests, InputStream stream, Map<String, List<String>> headers) throws Exception {
        List<JsonRpcResponseModel2> responses = null;

        JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));

        try {
            responses = gson.fromJson(reader,
                    new TypeToken<List<JsonRpcResponseModel2>>() {
                    }.getType());
        } catch (JsonSyntaxException ex) {
            throw new RequestException("Wrong server response. Did you select the correct protocol controller? Maybe your server doesn't support batch? Try JsonRpc2Controller.", ex);
        }

        reader.close();


        if (responses == null) {
            throw new RequestException("Empty server response.");
        }

        Collections.sort(responses);
        Collections.sort(requests, new Comparator<RequestInterface>() {
            @Override
            public int compare(RequestInterface lhs, RequestInterface rhs) {
                return lhs.getId().compareTo(rhs.getId());
            }
        });

        List<RequestResult> finalResponses = new ArrayList<RequestResult>(responses.size());


        for (int i = 0; i < responses.size(); i++) {
            JsonRpcResponseModel2 res = responses.get(i);
            if (res.jsonrpc == null) {
                throw new RequestException("Wrong server response. Did you select the correct protocol controller? Maybe your server doesn't support json-com.judocallbacks.controllers.json.rpc 2.0? Try JsonRpc1Controller.");
            } else if (res.error == null) {
                Object result = null;
                try {
                    Type type = requests.get(i).getReturnType();
                    if (!type.equals(Void.class)) {
                        result = gson.fromJson(res.result, requests.get(i).getReturnType());
                        if (!requests.get(i).isAllowEmptyResult() && result == null) {
                            finalResponses.add(new ErrorResult(requests.get(i).getId(), new RequestException("Empty response.")));
                        } else {
                            finalResponses.add(new RequestSuccessResult(res.id, result));
                        }
                    } else {
                        finalResponses.add(new RequestSuccessResult(res.id, null));
                    }
                } catch (JsonSyntaxException ex) {
                    finalResponses.add(new ErrorResult(res.id, new RequestException(requests.get(i).getName(), ex)));
                }

            } else {
                finalResponses.add(new ErrorResult(res.id, new RequestException(requests.get(i).getName() + ": " + res.error.message, res.error.code)));
            }
        }

        return finalResponses;
    }

    protected class JsonRpcRequestModel2 extends JsonRpcRequestModel {
        final String jsonrpc = "2.0";

        public JsonRpcRequestModel2(String method, Object params, Integer id) {
            super(method, params, id);
        }
    }

    protected class JsonRpcResponseModel2 extends JsonRpcResponseModel {
        JsonProtocolController.JsonErrorModel error;
        String jsonrpc;
    }


}
