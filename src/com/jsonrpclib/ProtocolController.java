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

    public enum ConnectionType
    {
        GET,POST
    }

    public class RequestInfo
    {
        public Object postRequest;
        public String url;
    }

    protected void longLog(String tag, String message)
    {
        JsonLoggerImpl.longLog(tag, message);
    }


    protected String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }



    public abstract RequestInfo createRequest(String url, JsonRequest request, String apiKey);
    public abstract JsonResult parseResponse(JsonRequest request, InputStream stream, int debugFlag, JsonTimeStat timeStat);
    public abstract ConnectionType getConnectionType();

    public boolean isBatchSupported() {
        return false;
    }

    public RequestInfo createRequest(String url, List<JsonRequest> requests,  String apiKey) throws Exception {
        throw new JsonException("CreateRequest not implemented.");
    }

    public List<JsonResult> parseResponses(List<JsonRequest> requests, InputStream stream, int debugFlag, JsonTimeStat timeStat) throws Exception {
        throw new JsonException("ParseResponses not implemented.");
    }

    public void writeToStream(Writer writer, Object request, int debugFlag) throws Exception{
        throw new JsonException("WriteToStream not implemented.");
    }
}
