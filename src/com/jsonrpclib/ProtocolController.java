package com.jsonrpclib;

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
        public Object data;
        public String mimeType;
    }


    public abstract RequestInfo createRequest(String url, JsonRequestInterface request, String apiKey);

    public abstract JsonResult parseResponse(JsonRequestInterface request, InputStream stream);

    public boolean isBatchSupported() {
        return false;
    }

    public RequestInfo createRequest(String url, List<JsonRequestInterface> requests, String apiKey) throws Exception {
        throw new JsonException("CreateRequest not implemented.");
    }

    public List<JsonResult> parseResponses(List<JsonRequestInterface> requests, InputStream stream) throws Exception {
        throw new JsonException("ParseResponses not implemented.");
    }

    public void writeToStream(Writer writer, Object request) throws Exception {
        throw new JsonException("WriteToStream not implemented.");
    }
}
