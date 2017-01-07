package com.github.kubatatami.judonetworking.observers;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.02.2013
 * Time: 11:01
 */
public interface WrapperObserver<T> {

    void onUpdate(T data);

}
