package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 */
public class Callback<T> implements CallbackInterface<T> {

    @Override
    public void onStart() {

    }

    @Override
    public void onSuccess(T result) {
    }

    @Override
    public void onError(JudoException e) {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onProgress(int progress) {
    }


}
