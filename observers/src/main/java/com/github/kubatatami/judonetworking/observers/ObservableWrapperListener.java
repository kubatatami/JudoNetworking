package com.github.kubatatami.judonetworking.observers;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 01.03.2013
 * Time: 13:28
 */
public class ObservableWrapperListener<T> implements ObservableWrapperListenerInterface<T> {

    @Override
    public void onGet(ObservableWrapper<T> wrapper) {

    }

    @Override
    public void onSet(ObservableWrapper<T> wrapper, T value) {

    }

    @Override
    public boolean onAddObserver(ObservableWrapper<T> wrapper, WrapperObserver<T> observer) {
        return true;
    }

    @Override
    public boolean onDeleteObserver(ObservableWrapper<T> wrapper, WrapperObserver<T> observer) {
        return true;
    }

    @Override
    public void onUpdate(ObservableWrapper<T> wrapper) {

    }

}
