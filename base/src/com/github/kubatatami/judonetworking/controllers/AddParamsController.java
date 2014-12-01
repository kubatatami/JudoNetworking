package com.github.kubatatami.judonetworking.controllers;

import com.github.kubatatami.judonetworking.internals.RequestInterface;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.reflect.Array;

/**
 * Created by Kuba on 04/03/14.
 */
public class AddParamsController extends ProtocolControllerWrapper {

    protected Object[] additionalParams;
    protected String[] paramNames;

    public AddParamsController(ProtocolController baseController, Object... additionalParams) {
        super(baseController);
        this.additionalParams = additionalParams;
    }

    public AddParamsController(ProtocolController baseController, String[] paramNames, Object... additionalParams) {
        super(baseController);
        if (paramNames.length != additionalParams.length) {
            throw new RuntimeException("Param names must be the same size like additionalParams.");
        }
        this.paramNames = paramNames;
        this.additionalParams = additionalParams;
    }

    @Override
    public RequestInfo createRequest(String url, RequestInterface request) throws JudoException {
        String[] newParamNames = concatenate(request.getParamNames(), paramNames);
        Object[] newArgs = concatenate(request.getArgs(), additionalParams);
        request.setArgs(newArgs);
        request.setParamNames(newParamNames);
        return super.createRequest(url, request);
    }

    protected <T> T[] concatenate(T[] a, T[] b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        int aLen = a.length;
        int bLen = b.length;
        @SuppressWarnings("unchecked")
        T[] C = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, C, 0, aLen);
        System.arraycopy(b, 0, C, aLen, bLen);

        return C;
    }

    public Object[] getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Object[] additionalParams) {
        this.additionalParams = additionalParams;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }
}
