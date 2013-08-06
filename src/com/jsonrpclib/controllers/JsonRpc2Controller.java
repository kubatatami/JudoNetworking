package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.google.gson22.reflect.TypeToken;
import com.google.gson22.stream.JsonReader;
import com.jsonrpclib.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class JsonRpc2Controller extends JsonRpcController {

    public JsonRpc2Controller() {
    }

    public JsonRpc2Controller(GsonBuilder builder) {
        super(builder);
    }


    @Override
    protected Object createRequestModel(String name, Object params, Integer id) {
        return new JsonRpcRequestModel2(name, params, id);
    }

    @Override
    public RequestInfo createRequest(String url, List<JsonRequest> requests, String apiKey) {
        int i = 0;
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url;
        Object[] requestsJson = new Object[requests.size()];
        for (JsonRequest request : requests) {
            requestsJson[i] = createRequest(url, request, apiKey).postRequest;
            i++;
        }
        requestInfo.postRequest = requestsJson;
        return requestInfo;
    }

    @Override
    public boolean isBatchSupported() {

        return true;
    }

    @Override
    public JsonResult parseResponse(JsonRequest request, InputStream stream, int debugFlag, JsonTimeStat timeStat) {
        try {
            JsonRpcResponseModel2 response = null;
            if ((debugFlag & JsonRpc.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(stream);
                longLog("RES(" + resStr.length() + ")", resStr);
                timeStat.tickReadTime();
                response = gson.fromJson(resStr, JsonRpcResponseModel2.class);
                if (response == null) {
                    throw new JsonException("Can't parse response.");
                } else if (response.error != null) {
                    throw new JsonException(response.error.message, response.error.code);
                }


            } else {
                JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
                response = gson.fromJson(reader, JsonRpcResponseModel2.class);
                timeStat.tickReadTime();
                if (response.error != null) {
                    throw new JsonException(response.error.message, response.error.code);
                }


                reader.close();

            }

            if (!request.getReturnType().equals(Void.TYPE)) {
                return new JsonResult(request.getId(),gson.fromJson(response.result, request.getReturnType()));
            }
            timeStat.tickParseTime();
            return new JsonResult(request.getId(),null);
        } catch (Exception e) {
            return new JsonResult(e);
        }
    }

    @Override
    public List<JsonResult> parseResponses(List<JsonRequest> requests, InputStream stream, int debugFlag, JsonTimeStat timeStat) throws Exception {
        List<JsonRpcResponseModel2> responses = null;

        if ((debugFlag & JsonRpc.RESPONSE_DEBUG) > 0) {

            String resStr = convertStreamToString(stream);
            timeStat.tickReadTime();
            longLog("RES(" + resStr.length() + ")", resStr);
            responses = gson.fromJson(resStr,
                    new TypeToken<List<JsonRpcResponseModel2>>() {
                    }.getType());
        } else {
            JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            responses = gson.fromJson(reader,
                    new TypeToken<List<JsonRpcResponseModel2>>() {
                    }.getType());
            reader.close();
            timeStat.tickReadTime();

        }

        if (responses == null) {
            throw new JsonException("Empty response.");
        }

        Collections.sort(responses);
        Collections.sort(requests, new Comparator<JsonRequest>() {
            @Override
            public int compare(JsonRequest lhs, JsonRequest rhs) {
                return lhs.getId().compareTo(rhs.getId());
            }
        });

        List<JsonResult> finalResponses = new ArrayList<JsonResult>(responses.size());


        for (int i = 0; i < responses.size(); i++) {
            JsonRpcResponseModel2 res = responses.get(i);
            if (res.error == null) {
                Object result = null;
                if (!requests.get(i).getReturnType().equals(Void.TYPE)) {
                    result = gson.fromJson(res.result, requests.get(i).getReturnType());
                }
                finalResponses.add(new JsonResult(res.id, result));
            } else {
                finalResponses.add(new JsonResult(res.id, new JsonException(res.error.message, res.error.code)));
            }
        }

        timeStat.tickParseTime();

        return finalResponses;
    }

    protected class JsonRpcRequestModel2 extends JsonRpcRequestModel {
        final String jsonrpc = "2.0";

        public JsonRpcRequestModel2(String method, Object params, Integer id) {
            super(method, params, id);
        }
    }

    protected class JsonRpcResponseModel2 extends JsonRpcResponseModel {
        JsonErrorModel error;
    }


}
