package com.jsonrpclib.connectors;

import android.os.Build;
import android.util.Base64;
import com.jsonrpclib.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 29.03.2013
 * Time: 13:14
 */
public class JsonHttpUrlConnection extends JsonConnection {

    private int reconnections = 3;
    private int connectTimeout = 15000;
    private int methodTimeout = 10000;
    private String authKey=null;
    private HttpURLCreator httpURLCreator = null;
    private HttpURLConnectionModifier httpURLConnectionModifier = null;


    public JsonHttpUrlConnection() {
        init(new HttpURLCreatorImplementation(), null);
    }

    public JsonHttpUrlConnection(HttpURLCreator httpURLCreator) {
        init(httpURLCreator, null);
    }

    public JsonHttpUrlConnection(HttpURLCreator httpURLCreator, HttpURLConnectionModifier httpURLConnectionModifier) {
        init(httpURLCreator, httpURLConnectionModifier);
    }

    private void init(HttpURLCreator httpURLCreator, HttpURLConnectionModifier httpURLConnectionModifier)
    {
        this.httpURLCreator=httpURLCreator;
        this.httpURLConnectionModifier=httpURLConnectionModifier;
        disableConnectionReuseIfNecessary();
    }

    private void disableConnectionReuseIfNecessary() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }


    public void setHttpURLCreator(HttpURLCreator httpURLCreator) {
        this.httpURLCreator = httpURLCreator;
    }

    public void setHttpURLConnectionModifier(HttpURLConnectionModifier httpURLConnectionModifier) {
        this.httpURLConnectionModifier = httpURLConnectionModifier;
    }

    public void setBasicAuthentication(final String username, final String password) {
        authKey = auth(username, password);
    }

    private String auth(String login, String pass) {
        String source = login + ":" + pass;
        return "Basic " + Base64.encodeToString(source.getBytes(), Base64.NO_WRAP);
    }

    public Connection send(ProtocolController protocolController, ProtocolController.RequestInfo requestInfo,
                           int timeout, JsonTimeStat timeStat, int debugFlags) throws Exception {

        HttpURLConnection urlConnection = null;
        if(requestInfo.data ==null)
        {
            urlConnection = get(requestInfo.url,  timeout, timeStat, debugFlags);

        }
        else
        {
            urlConnection = post(protocolController, requestInfo, timeout, timeStat, debugFlags);
        }

        final HttpURLConnection finalConnection = urlConnection;
        return new Connection() {
            @Override
            public InputStream getStream() throws IOException {
                return finalConnection.getInputStream();
            }

            @Override
            public void close() {
                finalConnection.disconnect();
            }
        };

    }

    public HttpURLConnection get(String url, int timeout, JsonTimeStat timeStat, int debugFlags) throws Exception {
        HttpURLConnection urlConnection = null;

        if ((debugFlags & JsonRpc.REQUEST_DEBUG) > 0) {
            longLog("REQ", url);
        }

        for (int i = 1; i <= reconnections; i++) {
            try {
                urlConnection = httpURLCreator.create(url);
                break;
            } catch (IOException e) {
                if (i == reconnections) {
                    throw e;
                }
            }
        }
        if (urlConnection != null) {
            if (authKey != null) {
                urlConnection.addRequestProperty("Authorization", authKey);
            }

            urlConnection.setConnectTimeout(connectTimeout);
            if (timeout == 0) {
                timeout = methodTimeout;
            }
            urlConnection.setReadTimeout(timeout);
            timeStat.setTimeout(timeout);


            if (httpURLConnectionModifier != null) {
                httpURLConnectionModifier.modify(urlConnection);
            }

            urlConnection.getInputStream();
            timeStat.tickConnectionTime();
        }
        return urlConnection;
    }


    public HttpURLConnection post(ProtocolController protocolController, ProtocolController.RequestInfo requestInfo, int timeout, JsonTimeStat timeStat, int debugFlags) throws Exception {
        HttpURLConnection urlConnection = null;


        for (int i = 1; i <= reconnections; i++) {
            try {
                urlConnection = httpURLCreator.create(requestInfo.url);
                break;
            } catch (IOException e) {
                if (i == reconnections) {
                    throw e;
                }
            }
        }

        if(requestInfo.mimeType!=null)
        {
            urlConnection.addRequestProperty("Content-Type", requestInfo.mimeType);
        }

        if (authKey != null) {
            urlConnection.addRequestProperty("Authorization", authKey);
        }

        urlConnection.setConnectTimeout(connectTimeout);
        if (timeout == 0) {
            timeout = methodTimeout;
        }

        timeStat.setTimeout(timeout);
        urlConnection.setReadTimeout(timeout);
        urlConnection.setDoOutput(true);

        if (httpURLConnectionModifier != null) {
            httpURLConnectionModifier.modify(urlConnection);
        }

        OutputStream stream = urlConnection.getOutputStream();
        timeStat.tickConnectionTime();
        Writer writer = new BufferedWriter(new OutputStreamWriter(stream));

        protocolController.writeToStream(writer, requestInfo.data, debugFlags);


        writer.close();
        timeStat.tickSendTime();
        return urlConnection;
    }

    @Override
    public void setMaxConnections(int max) {
        System.setProperty("http.maxConnections", max + "");
    }


    public void setReconnections(int reconnections) {
        this.reconnections = reconnections;
    }


    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getMethodTimeout() {
        return methodTimeout;
    }

    public void setMethodTimeout(int methodTimeout) {
        this.methodTimeout = methodTimeout;
    }

    public interface HttpURLCreator {

        public HttpURLConnection create(String url) throws IOException;

    }

    public interface HttpURLConnectionModifier {

        void modify(HttpURLConnection connection);

    }

    class HttpURLCreatorImplementation implements HttpURLCreator {

        @Override
        public HttpURLConnection create(String url) throws IOException {

            return (HttpURLConnection) new URL(url).openConnection();
        }

    }
}
