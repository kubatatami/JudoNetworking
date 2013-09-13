package com.jsonrpclib;

import com.jsonrpclib.connectors.JsonHttpUrlConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 22:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class ProtocolController {

    public static class RequestInfo {
        public String url;
        public JsonInputStreamEntity entity;
        public String mimeType;
    }


    public abstract RequestInfo createRequest(String url, JsonRequestInterface request) throws Exception;

    public abstract JsonResult parseResponse(JsonRequestInterface request, InputStream stream);

    public boolean isBatchSupported() {
        return false;
    }

    public RequestInfo createRequest(String url, List<JsonRequestInterface> requests) throws Exception {
        throw new JsonException("CreateRequest not implemented.");
    }

    public List<JsonResult> parseResponses(List<JsonRequestInterface> requests, InputStream stream) throws Exception {
        throw new JsonException("ParseResponses not implemented.");
    }

    public void parseError(int code, String resp) throws Exception {

    }


}
