package com.github.kubatatami.judonetworking.internals.requests;

import com.github.kubatatami.judonetworking.annotations.RequestMethod;

import java.lang.annotation.Annotation;

/**
 * Created by Kuba on 09/04/14.
 */
public class RequestOptions implements RequestMethod {

    private String[] paramNames = new String[0];

    private int timeout = 0;

    private boolean allowEmptyResult = false;

    private boolean isApiKeyRequired = false;

    public RequestOptions() {
    }

    public RequestOptions setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
        return this;
    }

    public RequestOptions setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public RequestOptions setAllowEmptyResult(boolean allowEmptyResult) {
        this.allowEmptyResult = allowEmptyResult;
        return this;
    }

    public RequestOptions setApiKeyRequired(boolean isApiKeyRequired) {
        this.isApiKeyRequired = isApiKeyRequired;
        return this;
    }

    @Override
    public String[] paramNames() {
        return paramNames;
    }

    @Override
    public int timeout() {
        return timeout;
    }

    @Override
    public boolean allowEmptyResult() {
        return allowEmptyResult;
    }

    @Override
    public Class<? extends Modifier> modifier() {
        return DefaultModifier.class;
    }

    public boolean apiKeyRequired() {
        return isApiKeyRequired;
    }

    @Override
    public int id() {
        return 0;
    }

    @Override
    public final String name() {
        return null;
    }

    @Override
    public final boolean async() {
        return false;
    }

    @Override
    public final boolean highPriority() {
        return false;
    }

    @Override
    public final Class<? extends Annotation> annotationType() {
        return RequestMethod.class;
    }

    @Override
    public final boolean equals(Object o) {
        return this == o;
    }

    @Override
    public final int hashCode() {
        return 0;
    }

    @Override
    public final String toString() {
        return "";
    }
}
