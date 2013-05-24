package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 19.02.2013
 * Time: 12:06
 *
 */
public class JsonLogger {


    public static void setTag(String tag) {
        JsonLoggerImpl.setTag(tag);
    }

    public static void setLevel(JsonLogLevel level) {
        JsonLoggerImpl.setLevel(level);
    }
}
