package com.github.kubatatami.judonetworking.internals.wear;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuba on 28/09/15.
 */
public class WearResponse {

    private Map<String, List<String>> headers;

    private int code;

    private byte[] body;

    private boolean successful;

    private String message;

    public WearResponse() {
    }

    public WearResponse(Exception exception) {
        this.message = exception.getMessage();
        this.headers = new HashMap<>();
        this.successful = false;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public int getCode() {
        return code;
    }

    public String getHeader(String key) {
        if (headers.containsKey(key)) {
            return headers.get(key).get(0);
        } else {
            return null;
        }
    }

    public void setCode(int code) {
        this.code = code;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getHeaders(String name) {
        return headers.get(name);
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
