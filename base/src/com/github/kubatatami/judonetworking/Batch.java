package com.github.kubatatami.judonetworking;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public abstract class Batch<T> implements BatchInterface<T> {

    @Override
    public void run(final T api) {

    }

    @Override
    public void runNonFatal(final T api) {
    }

    @Override
    public void onSuccess(Object[] results) {
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onProgress(int progress) {
    }

    @Override
    public void onFinish() {

    }
}
