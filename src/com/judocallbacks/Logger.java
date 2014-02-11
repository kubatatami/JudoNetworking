package com.judocallbacks;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 19.02.2013
 * Time: 12:06
 */
public class Logger {


    public static void setTag(String tag) {
        LoggerImpl.setTag(tag);
    }

    public static void setLevel(LogLevel level) {
        LoggerImpl.setLevel(level);
    }
}
