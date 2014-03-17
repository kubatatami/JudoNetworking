package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 29.05.2013
 * Time: 10:07
 * To change this template use File | Settings | File Templates.
 */
public interface Clonner {


    public <T> T clone(T object) throws JudoException;


}
