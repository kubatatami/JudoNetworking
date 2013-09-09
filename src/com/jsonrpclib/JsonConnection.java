package com.jsonrpclib;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 24.03.2013
 * Time: 21:53
 */
public abstract class JsonConnection {

    public abstract Connection send(ProtocolController protocolController, ProtocolController.RequestInfo requestInfo,
                                    int timeout, JsonTimeStat timeStat, int debugFlags, Method method) throws Exception;

    public abstract void setMaxConnections(int max);

    public abstract void setReconnections(int reconnections);

    public abstract void setConnectTimeout(int connectTimeout);

    public abstract void setMethodTimeout(int methodTimeout);

    public abstract int getMethodTimeout();


    public interface Connection {
        public InputStream getStream() throws Exception;

        public int getContentLength();

        public void close();
    }

    protected void longLog(String tag, String message) {
        JsonLoggerImpl.longLog(tag, message);
    }

    protected String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
