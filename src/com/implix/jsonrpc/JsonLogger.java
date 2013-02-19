package com.implix.jsonrpc;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 19.02.2013
 * Time: 12:06
 * To change this template use File | Settings | File Templates.
 */
public class JsonLogger {


    public static void setTag(String tag) {
        JsonLoggerImpl.setTag(tag);
    }

    public static void setLevel(LogLevel level) {
        JsonLoggerImpl.setLevel(level);
    }
}
