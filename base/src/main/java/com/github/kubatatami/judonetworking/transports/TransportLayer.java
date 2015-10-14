package com.github.kubatatami.judonetworking.transports;

import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.stats.TimeStat;
import com.github.kubatatami.judonetworking.logs.JudoLogger;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 24.03.2013
 * Time: 21:53
 */
public abstract class TransportLayer {

    public abstract Connection send(String requestName, ProtocolController protocolController, ProtocolController.RequestInfo requestInfo,
                                    int timeout, TimeStat timeStat, int debugFlags, Method method) throws JudoException;

    public abstract void setMaxConnections(int max);

    public abstract void setConnectTimeout(int connectTimeout);

    public abstract void setMethodTimeout(int methodTimeout);

    public abstract int getMethodTimeout();


    public interface Connection {

        InputStream getStream() throws ConnectionException;

        int getContentLength();

        Long getDate();

        void close();

        Map<String, List<String>> getHeaders();
    }


    protected void longLog(String tag, String message, JudoLogger.LogLevel level) {
        JudoLogger.longLog(tag, message, level);
    }

    protected static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
