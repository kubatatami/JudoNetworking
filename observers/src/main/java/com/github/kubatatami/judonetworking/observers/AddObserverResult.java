package com.github.kubatatami.judonetworking.observers;

public class AddObserverResult<T> {

    private final ObservableWrapper<T> observableWrapper;

    private final WrapperObserver<T> observer;

    public AddObserverResult(ObservableWrapper<T> observableWrapper, WrapperObserver<T> observer) {
        this.observableWrapper = observableWrapper;
        this.observer = observer;
    }

    public AddObserverResult<T> deleteObserverOnDestroy(ObservableController controller) {
        controller.addObserverToDelete(observableWrapper, observer);
        return this;
    }

    public AddObserverResult<T> notifyObservers() {
        observableWrapper.notifyObservers();
        return this;
    }
}
