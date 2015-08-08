package com.github.kubatatami.judonetworking.logs;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 19.02.2013
 * Time: 11:47
 */
public class JudoLogger {


    private static String tag = JudoLogger.class.getPackage().getName();


    public static void log(String text, LogLevel level) {
        switch (level) {
            case ASSERT:
                Log.wtf(tag, text);
                break;
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
            case VERBOSE:
                Log.v(tag, text);
                break;
        }
    }

    public static void log(Exception ex) {
        if (ex != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            JudoLogger.log(sw.toString(), LogLevel.ERROR);
        } else {
            JudoLogger.log("Null exception", LogLevel.ERROR);
        }
    }

    public static void setTag(String tag) {
        JudoLogger.tag = tag;
    }


    public static synchronized void longLog(String tag, String str, LogLevel level) {
        JudoLogger.log(tag + ":", level);
        int i;
        for (i = 0; i < str.length() - 256; i += 256) {
            JudoLogger.log(str.substring(i, i + 256), level);
        }
        JudoLogger.log(str.substring(i, str.length()), level);

    }

    public enum LogLevel {
        ASSERT, ERROR, WARNING, INFO, DEBUG, VERBOSE
    }
}
