package com.jsonrpclib;

import com.google.gson22.Gson;
import com.google.gson22.JsonElement;
import com.jsonrpclib.JsonRequest;
import com.jsonrpclib.JsonResult;
import com.jsonrpclib.JsonTimeStat;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 22:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class ProtocolController {

    public static class RequestInfo
    {
        public String url;
        public Object data;
        public String mimeType;
    }

    protected void longLog(String tag, String message)
    {
        JsonLoggerImpl.longLog(tag, message);
    }


    protected String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }



    public abstract RequestInfo createRequest(String url, JsonRequestInterface request, String apiKey);
    public abstract JsonResult parseResponse(JsonRequestInterface request, InputStream stream, int debugFlag, JsonTimeInterface timeStat);

    public boolean isBatchSupported() {
        return false;
    }

    public RequestInfo createRequest(String url, List<JsonRequestInterface> requests,  String apiKey) throws Exception {
        throw new JsonException("CreateRequest not implemented.");
    }

    public List<JsonResult> parseResponses(List<JsonRequestInterface> requests, InputStream stream, int debugFlag, JsonTimeInterface timeStat) throws Exception {
        throw new JsonException("ParseResponses not implemented.");
    }

    public void writeToStream(Writer writer, Object request, int debugFlag) throws Exception{
        throw new JsonException("WriteToStream not implemented.");
    }
}
