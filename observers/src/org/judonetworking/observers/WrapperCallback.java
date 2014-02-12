package org.judonetworking.observers;

import org.judonetworking.Callback;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 */
public class WrapperCallback<T> extends Callback<T> {

    protected final ObservableWrapper<T> wrapper;

    public WrapperCallback(ObservableWrapper<T> wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void onFinish(T result) {
        wrapper.set(result);
    }

}
