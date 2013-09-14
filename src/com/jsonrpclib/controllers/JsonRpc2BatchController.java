package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.google.gson22.JsonSyntaxException;
import com.google.gson22.reflect.TypeToken;
import com.google.gson22.stream.JsonReader;
import com.jsonrpclib.*;

import java.io.*;
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
public class JsonRpc2BatchController extends JsonRpc2Controller {

    public JsonRpc2BatchController() {
    }

    public JsonRpc2BatchController(GsonBuilder builder) {
        super(builder);
    }

    @Override
    public RequestInfo createRequest(String url, List<JsonRequestInterface> requests) throws Exception {
        int i = 0;
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.url = url;
        Object[] requestsJson = new Object[requests.size()];
        for (JsonRequestInterface request : requests) {
            requestsJson[i] = createRequestObject(request);
            i++;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        gson.toJson(requestsJson, writer);
        writer.close();
        requestInfo.entity = new JsonInputStreamEntity(new ByteArrayInputStream(stream.toByteArray()), stream.size());
        requestInfo.mimeType = "application/json";
        return requestInfo;
    }

    @Override
    public boolean isBatchSupported() {

        return true;
    }

    @Override
    public List<JsonResult> parseResponses(List<JsonRequestInterface> requests, InputStream stream) throws Exception {
        List<JsonRpcResponseModel2> responses = null;

        JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
        responses = gson.fromJson(reader,
                new TypeToken<List<JsonRpcResponseModel2>>() {
                }.getType());
        reader.close();


        if (responses == null) {
            throw new JsonException("Empty response.");
        }

        Collections.sort(responses);
        Collections.sort(requests, new Comparator<JsonRequestInterface>() {
            @Override
            public int compare(JsonRequestInterface lhs, JsonRequestInterface rhs) {
                return lhs.getId().compareTo(rhs.getId());
            }
        });

        List<JsonResult> finalResponses = new ArrayList<JsonResult>(responses.size());


        for (int i = 0; i < responses.size(); i++) {
            JsonRpcResponseModel2 res = responses.get(i);
            if (res.error == null) {
                Object result = null;
                if (!requests.get(i).getReturnType().equals(Void.TYPE)) {
                    try {
                        result = gson.fromJson(res.result, requests.get(i).getReturnType());
                        finalResponses.add(new JsonSuccessResult(res.id, result));
                    } catch (JsonSyntaxException ex) {
                        finalResponses.add(new JsonErrorResult(res.id, new JsonException(requests.get(i).getName(), ex)));
                    }
                }
            } else {
                finalResponses.add(new JsonErrorResult(res.id, new JsonException(requests.get(i).getName() + ": " + res.error.message, res.error.code)));
            }
        }

        return finalResponses;
    }


}
