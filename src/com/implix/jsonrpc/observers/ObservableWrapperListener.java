package com.implix.jsonrpc.observers;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 01.03.2013
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class ObservableWrapperListener<T>  implements ObservableWrapperListenerInterface<T>{

    @Override
    public void onGet(ObservableWrapper<T> wrapper) {

    }

    @Override
    public void onSet(ObservableWrapper<T> wrapper, T value) {

    }

    @Override
    public boolean onAddObserver(ObservableWrapper<T> wrapper, WrapObserver<T> observer) {
        return true;
    }

    @Override
    public boolean onDeleteObserver(ObservableWrapper<T> wrapper, WrapObserver<T> observer) {
        return true;
    }

    @Override
    public void onUpdate(ObservableWrapper<T> wrapper) {

    }

}
