package org.judonetworking.observers;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 01.03.2013
 * Time: 13:28
 */
public interface ObservableWrapperListenerInterface<T> {

    public void onGet(ObservableWrapper<T> wrapper);

    public void onSet(ObservableWrapper<T> wrapper, T value);

    public boolean onAddObserver(ObservableWrapper<T> wrapper, WrapObserver<T> observer);

    public boolean onDeleteObserver(ObservableWrapper<T> wrapper, WrapObserver<T> observer);

    public void onUpdate(ObservableWrapper<T> wrapper);

}
