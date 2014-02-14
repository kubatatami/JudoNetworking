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
        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            LoggerImpl.log(sw.toString());
        } else {
            LoggerImpl.log("Null exception");
        }
    }

    @Override
    public void onProgress(int progress) {
    }


}
