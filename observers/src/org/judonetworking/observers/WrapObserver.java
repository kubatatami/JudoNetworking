package org.judonetworking.observers;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.02.2013
 * Time: 11:01
 */
public interface WrapObserver<T> {

    public void update(T data);

}
