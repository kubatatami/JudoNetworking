package com.github.kubatatami.judonetworking.observers;

public interface ObservableController {

    void addObserverToDelete(ObservableWrapper<?> observableWrapper, WrapperObserver<?> observer);

}
