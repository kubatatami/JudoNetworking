package com.jsonrpclib;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 */
public abstract class JsonBatch<T> implements JsonBatchInterface<T> {

    @Override
    public abstract void run(final T api);

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
        JsonLoggerImpl.log(sw.toString());
    }

    @Override
    public void onProgress(int progress) {
    }

}
