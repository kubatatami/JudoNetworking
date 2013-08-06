package com.jsonrpclib;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 24.03.2013
 * Time: 21:53
 *
 */
public abstract class JsonConnection {

    public abstract Connection send(ProtocolController protocolController,ProtocolController.RequestInfo requestInfo,
                           int timeout,JsonTimeStat timeStat, int debugFlags) throws Exception;

    public abstract void setMaxConnections(int max);

    public abstract void setReconnections(int reconnections);
    public abstract void setConnectTimeout(int connectTimeout);
    public abstract void setMethodTimeout(int methodTimeout);
    public abstract int getMethodTimeout();

    protected void longLog(String tag, String message)
    {
        JsonLoggerImpl.longLog(tag, message);
    }

    public interface Connection
    {
        public InputStream getStream() throws IOException;
        public void close();
    }

}
