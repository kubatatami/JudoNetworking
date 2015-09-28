package com.github.kubatatami.judonetworking.internals.wear;

import java.util.HashMap;
import java.util.Map;

public class WearRequest {

    private byte[] body;

    private boolean followRedirects;

    private Map<String, String> headers = new HashMap<>();

    private int connectTimeout;

    private int readTimeout;

    private String url;

    private String mimeType;

    private String methodName;

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public String getUrl() {
        return url;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}