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
public interface RequestInterface {

    public Integer getId();

    public String getName();

    public Object[] getArgs();

    public Type getReturnType();

    public String[] getParamNames();

    public Method getMethod();

    public boolean isAllowEmptyResult();

    public Object getAdditionalData();

    public boolean isApiKeyRequired();

    public void setArgs(Object[] args);

    public void setParamNames(String[] paramNames);

    public boolean isCancelled();

}
