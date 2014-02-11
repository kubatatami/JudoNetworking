package com.judocallbacks;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public abstract class Batch<T> implements BatchInterface<T> {

    @Override
    public void run(final T api){

    }

    @Override
    public void runNonFatal(final T api) {
    }

    @Override
    public void onFinish(Object[] results) {
    }

    @Override
    public void onError(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        LoggerImpl.log(sw.toString());
    }

    @Override
    public void onProgress(int progress) {
    }

}
