package com.github.kubatatami.judonetworking.observers;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 01.03.2013
 * Time: 13:28
 */
public interface ObservableWrapperListenerInterface<T> {

    void onGet(ObservableWrapper<T> wrapper);

    void onSet(ObservableWrapper<T> wrapper, T value);

    boolean onAddObserver(ObservableWrapper<T> wrapper, WrapperObserver<T> observer);

    boolean onDeleteObserver(ObservableWrapper<T> wrapper, WrapperObserver<T> observer);

    void onUpdate(ObservableWrapper<T> wrapper);

}
