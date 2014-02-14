package com.github.kubatatami.judonetworking;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
        public RequestInputStreamEntity entity;
        public String mimeType;
        public Map<String,String> customHeaders;
    }


    public abstract RequestInfo createRequest(String url, RequestInterface request) throws Exception;

    public abstract RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers);

    public boolean isBatchSupported() {
        return false;
    }

    public RequestInfo createRequest(String url, List<RequestInterface> requests) throws Exception {
        throw new RequestException("CreateRequest not implemented.");
    }

    public List<RequestResult> parseResponses(List<RequestInterface> requests, InputStream stream, Map<String, List<String>> headers) throws Exception {
        throw new RequestException("ParseResponses not implemented.");
    }

    public void parseError(int code, String resp) throws Exception {

    }

    public abstract Object getAdditionalRequestData();


}
