package com.jsonrpclib;

import android.os.Build;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 29.03.2013
 * Time: 13:14
 *
 */
class JsonHttpUrlConnection implements JsonConnection {

    private final JsonRpcImplementation rpc;
    private int reconnections = 3;
    private int connectTimeout = 15000;
    private int methodTimeout = 10000;
    private float percentLoss=0.0f;
    private Random randomGenerator = new Random();

    public JsonHttpUrlConnection(JsonRpcImplementation rpc) {
        this.rpc = rpc;
        disableConnectionReuseIfNecessary();
    }

    private void disableConnectionReuseIfNecessary() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private void lossCheck() throws JsonException
    {
        if(percentLoss!=0 && randomGenerator.nextFloat() < percentLoss)
        {
            throw new JsonException("Random package lost.");
        }
    }

    public void setPercentLoss(float percentLoss) {
        this.percentLoss = percentLoss;
    }

    @Override
    public HttpURLConnection get(String url, int timeout, JsonTimeStat timeStat) throws Exception {
        HttpURLConnection urlConnection = null;

        lossCheck();

        for (int i = 1; i <= reconnections; i++) {
            try {
                urlConnection = rpc.getHttpURLCreator().create(url);
                break;
            } catch (IOException e) {
                if (i == reconnections) {
                    throw e;
                }
            }
        }
        if (urlConnection != null) {
            if (rpc.getAuthKey() != null) {
                urlConnection.addRequestProperty("Authorization", rpc.getAuthKey());
            }

            urlConnection.setConnectTimeout(connectTimeout);
            if (timeout == 0) {
                timeout = methodTimeout;
            }
            urlConnection.setReadTimeout(timeout);
            timeStat.setTimeout(timeout);

            if ((rpc.getDebugFlags() & JsonRpc.REQUEST_DEBUG) > 0) {
                JsonLoggerImpl.longLog("REQ(GET)", url);
            }

            if(rpc.getHttpURLConnectionModifier()!=null)
            {
                rpc.getHttpURLConnectionModifier().modify(urlConnection);
            }

            urlConnection.getInputStream();
            timeStat.tickConnectionTime();
        }
        return urlConnection;
    }

    @Override
    public HttpURLConnection post(ProtocolController protocolController,String url, Object request, int timeout, JsonTimeStat timeStat) throws Exception {
        HttpURLConnection urlConnection = null;

        lossCheck();

        for (int i = 1; i <= reconnections; i++) {
            try {
                urlConnection = rpc.getHttpURLCreator().create(url);
                break;
            } catch (IOException e) {
                if (i == reconnections) {
                    throw e;
                }
            }
        }

        urlConnection.addRequestProperty("Content-Type", "application/json");

        if (rpc.getAuthKey() != null) {
            urlConnection.addRequestProperty("Authorization", rpc.getAuthKey());
        }

        urlConnection.setConnectTimeout(connectTimeout);
        if (timeout == 0) {
            timeout = methodTimeout;
        }

        timeStat.setTimeout(timeout);
        urlConnection.setReadTimeout(timeout);
        urlConnection.setDoOutput(true);

        if(rpc.getHttpURLConnectionModifier()!=null)
        {
            rpc.getHttpURLConnectionModifier().modify(urlConnection);
        }

        OutputStream stream = urlConnection.getOutputStream();
        timeStat.tickConnectionTime();
        Writer writer = new BufferedWriter(new OutputStreamWriter(stream));

        protocolController.writeToStream(writer,request,rpc.getDebugFlags());


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
}
