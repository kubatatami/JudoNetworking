package com.github.kubatatami.judonetworking.observers;

public class AddObserverResult<T> {

    private final ObservableWrapper<T> observableWrapper;

    private final WrapperObserver<T> observer;

    public AddObserverResult(ObservableWrapper<T> observableWrapper, WrapperObserver<T> observer) {
        this.observableWrapper = observableWrapper;
        this.observer = observer;
    }

    public AddObserverResult<T> deleteOnDestroy(ObservableController controller) {
        controller.addObserverToDelete(observableWrapper, observer);
        return this;
    }

    public AddObserverResult<T> notifyNow() {
        observer.onUpdate(observableWrapper.get());
        return this;
    }
}
