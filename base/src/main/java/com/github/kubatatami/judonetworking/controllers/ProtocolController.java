package com.github.kubatatami.judonetworking.controllers;

import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.internals.streams.RequestInputStreamEntity;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 22:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class ProtocolController {

    private String apiKey = null;

    private String apiKeyName = null;

    public static class RequestInfo {

        public String url;

        public RequestInputStreamEntity entity;

        public String mimeType;

        public Map<String, String> customHeaders;
    }

    public void setApiKey(String name, String key) {
        this.apiKeyName = name;
        this.apiKey = key;
    }

    public void setApiKey(String key) {
        this.apiKey = key;
    }

    public int getAutoBatchTime() {
        return 0;
    }

    public abstract RequestInfo createRequest(String url, Request request) throws JudoException;

    public abstract RequestResult parseResponse(Request request, InputStream stream, Map<String, List<String>> headers);

    public boolean isBatchSupported() {
        return false;
    }

    public RequestInfo createRequests(String url, List<Request> requests) throws JudoException {
        throw new UnsupportedOperationException("CreateRequests not implemented.");
    }

    public List<RequestResult> parseResponses(List<Request> requests, InputStream stream, Map<String, List<String>> headers) throws JudoException {
        throw new UnsupportedOperationException("ParseResponses not implemented.");
    }

    public void parseError(int code, String resp) throws JudoException {

    }

    public Serializable getAdditionalRequestData() {
        return new ApiKey(apiKeyName, apiKey);
    }

    public TokenCaller getTokenCaller() {
        return null;
    }

    public static class ApiKey implements Serializable {

        private static final long serialVersionUID = -630342577502897709L;

        public String apiKeyName = null;

        public String apiKey = null;


        public ApiKey(String apiKeyName, String apiKey) {
            this.apiKeyName = apiKeyName;
            this.apiKey = apiKey;
        }
    }

    protected static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Created by Kuba on 21/02/14.
     */
    public abstract static class TokenCaller {

        public abstract long doTokenRequest(Endpoint endpoint) throws Exception;

        public boolean checkIsTokenException(Exception exception) {
            return false;
        }

    }
}
