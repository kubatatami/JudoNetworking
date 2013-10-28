package com.jsonrpclib.controllers;

import com.jsonrpclib.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 21.10.2013
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public abstract class SimpleController extends ProtocolController {

    protected String apiKey = null;
    protected String apiKeyName = null;

    public void setApiKey(String name, String key) {
        this.apiKeyName = name;
        this.apiKey = key;
    }

    public void setApiKey(String key) {
        this.apiKey = key;
    }


    @Override
    public abstract RequestInfo createRequest(String url, JsonRequestInterface request) throws Exception;

    @Override
    public JsonResult parseResponse(JsonRequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        if (request.getReturnType().equals(String.class)) {
            return new JsonSuccessResult(request.getId(), convertStreamToString(stream));
        } else if (request.getReturnType().equals(InputStream.class)) {
            return new JsonSuccessResult(request.getId(), stream);
        } else if (request.getReturnType().equals(Byte[].class)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];
            try {
                while ((nRead = stream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return new JsonSuccessResult(request.getId(), buffer.toByteArray());
            } catch (Exception e) {
                return new JsonErrorResult(request.getId(), e);
            }
        } else {
            return new JsonErrorResult(request.getId(), new JsonException("SimpleController handle string, byte array or input stream response only."));
        }
    }

    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public void parseError(int code, String resp) throws Exception {
        if (code == 405) {
            throw new JsonException("Server response: Method Not Allowed. Did you select the correct protocol controller?", new HttpException(resp, code));
        }
    }
}
