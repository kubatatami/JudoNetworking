package com.implix.jsonrpc;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public class JsonCallback<T> implements JsonCallbackInterface<T>{

    public void onFinish(T result)
    {

    }

    public void onError(Exception e)
    {
        if(e!=null)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JsonLoggerImpl.log(sw.toString());
        }
        else
        {
            JsonLoggerImpl.log("Null exception");
        }
    }

}
