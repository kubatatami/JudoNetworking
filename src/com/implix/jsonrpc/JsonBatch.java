package com.implix.jsonrpc;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 11.02.2013
 * Time: 22:48
 * To change this template use File | Settings | File Templates.
 */
public class JsonBatch<T> {

    public void run(final T api) {

    }

    public void onFinish(Object[] results) {

    }

    public void onError(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        JsonLoggerImpl.log(sw.toString());
    }

}
