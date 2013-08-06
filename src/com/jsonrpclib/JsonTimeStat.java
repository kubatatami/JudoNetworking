package com.jsonrpclib;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 30.03.2013
 * Time: 11:10
 */
public class JsonTimeStat  {
    private long allTime = 0;
    private long createTime = 0;
    private long connectionTime = 0;
    private long sendTime = 0;
    private long readTime = 0;
    private long parseTime = 0;
    private long time = 0;
    private long startTime = 0;
    private long timeout = 0;
    private List<JsonProgressObserver> requests;

    public final static int TICKS=5;

    public JsonTimeStat() {
        time = System.currentTimeMillis();
        startTime = time;
    }

    public JsonTimeStat(JsonProgressObserver request) {
        this.requests = new ArrayList<JsonProgressObserver>();
        requests.add(request);
        time = System.currentTimeMillis();
        startTime = time;
    }

    public <T extends JsonProgressObserver> JsonTimeStat(List<T> requests) {
        this.requests = new ArrayList<JsonProgressObserver>(requests);
        time = System.currentTimeMillis();
        startTime = time;
    }

    public void tickCacheTime() {
        for(int i=0;i<TICKS;i++)
        {
            progressTick();
        }
    }

    public void tickCreateTime() {
        createTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
        progressTick();
    }

    public void tickConnectionTime() {
        connectionTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
        progressTick();
    }

    public void tickSendTime() {
        sendTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
        progressTick();
    }

    public void tickReadTime() {
        readTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
        progressTick();
    }

    public void tickParseTime() {
        parseTime = System.currentTimeMillis() - time;
        progressTick();
    }

    public void tickEndTime() {
        allTime = System.currentTimeMillis() - startTime;
    }

    long getAllTime() {
        return allTime;
    }

    long getCreateTime() {
        return createTime;
    }

    long getConnectionTime() {
        return connectionTime;
    }

    long getReadTime() {
        return readTime;
    }

    long getParseTime() {
        return parseTime;
    }

    long getSendTime() {
        return sendTime;
    }

    long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getMethodTime() {
        return allTime - connectionTime;
    }

    public void logTime(String text) {
        JsonLoggerImpl.log(text +
                " create(" + (getCreateTime()) + "ms)" +
                " connection(" + getConnectionTime() + "ms)" +
                " timeout(" + getTimeout() + "ms)" +
                " send(" + getSendTime() + "ms)" +
                " read(" + getReadTime() + "ms) parse(" + getParseTime() + "ms)" +
                " all(" + getAllTime() + "ms)");
    }

    private void progressTick() {
        if (requests != null) {
            for (JsonProgressObserver request : requests) {
                request.progressTick();
            }
        }
    }
}
