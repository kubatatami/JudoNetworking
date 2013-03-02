package com.implix.jsonrpc.observers;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 01.03.2013
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class ObservableWrapperListener<T> {


    public void onGet(ObservableWrapper<T> wrapper) {

    }

    public void onSet(ObservableWrapper<T> wrapper, T value) {

    }


    public boolean onAddObserver(ObservableWrapper<T> wrapper, WrapObserver<T> observer) {
        return true;
    }

    public boolean onDeleteObserver(ObservableWrapper<T> wrapper, WrapObserver<T> observer) {
        return true;
    }


}
