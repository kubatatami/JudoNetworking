package com.implix.jsonrpc;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 19.02.2013
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */
class JsonLoggerImpl {


    private static String tag="com.implix.jsonrpc";
    private static LogLevel level=LogLevel.WARNING;



    public static void log(String text)
    {
        switch (level) {

            case ERROR:
                Log.e(tag, text);
                break;
            case WARNING:
                Log.w(tag, text);
                break;
            case INFO:
                Log.i(tag, text);
                break;
            case DEBUG:
                Log.d(tag, text);
                break;
        }
    }

    public static void log(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        JsonLoggerImpl.log(sw.toString());
    }

    public static void setTag(String tag) {
        JsonLoggerImpl.tag = tag;
    }

    public static void setLevel(LogLevel level) {
        JsonLoggerImpl.level = level;
    }
}
