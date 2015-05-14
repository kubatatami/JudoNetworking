package com.github.kubatatami.judonetworking;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 12.08.2013
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */
public interface Request {

    Integer getId();

    String getName();

    Object[] getArgs();

    Type getReturnType();

    boolean isVoidResult();

    String[] getParamNames();

    boolean isAllowEmptyResult();

    Object getAdditionalData();

    boolean isApiKeyRequired();

    void setArgs(Object[] args);

    void setParamNames(String[] paramNames);

    boolean isCancelled();

    int getMethodId();

    Method getMethod();
}
