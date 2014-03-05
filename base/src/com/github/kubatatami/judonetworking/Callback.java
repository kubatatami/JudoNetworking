package com.github.kubatatami.judonetworking;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 */
public class Callback<T> implements CallbackInterface<T> {

    @Override
    public void onFinish(T result) {
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onProgress(int progress) {
    }


}
