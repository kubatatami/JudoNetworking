package com.github.kubatatami.judonetworking.controllers;

import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.TokenCaller;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by Kuba on 04/03/14.
 */
public class ProtocolControllerWrapper extends ProtocolController {

    protected ProtocolController baseController;

    public ProtocolControllerWrapper(ProtocolController baseController) {
        this.baseController = baseController;
    }

    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws JudoException {
        return baseController.createRequest(url,request);
    }

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return baseController.parseResponse(request,stream,headers);
    }

    @Override
    public void setApiKey(String name, String key) {
        baseController.setApiKey(name,key);
    }

    @Override
    public void setApiKey(String key) {
        baseController.setApiKey(key);
    }

    @Override
    public int getAutoBatchTime() {
        return baseController.getAutoBatchTime();
    }

    @Override
    public boolean isBatchSupported() {
        return baseController.isBatchSupported();
    }

    @Override
    public RequestInfo createRequests(String url, List<RequestInterface> requests) throws JudoException {
        return baseController.createRequests(url, requests);
    }

    @Override
    public List<RequestResult> parseResponses(List<RequestInterface> requests, InputStream stream, Map<String, List<String>> headers) throws JudoException {
        return baseController.parseResponses(requests, stream, headers);
    }

    @Override
    public void parseError(int code, String resp) throws JudoException {
        baseController.parseError(code, resp);
    }

    @Override
    public Object getAdditionalRequestData() {
        return baseController.getAdditionalRequestData();
    }

    @Override
    public TokenCaller getTokenCaller() {
        return baseController.getTokenCaller();
    }

    public ProtocolController getBaseController() {
        return baseController;
    }
}
