package com.jsonrpclib;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 */
public class JsonCallback<T> implements JsonCallbackInterface<T> {

    @Override
    public void onFinish(T result) {
    }

    @Override
    public void onError(Exception e) {
        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JsonLoggerImpl.log(sw.toString());
        } else {
            JsonLoggerImpl.log("Null exception");
        }
    }

    @Override
    public void onProgress(int progress) {
    }


}
