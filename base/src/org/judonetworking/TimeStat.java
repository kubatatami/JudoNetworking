package org.judonetworking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 30.03.2013
 * Time: 11:10
 */
public class TimeStat implements TimeInterface {
    private long allTime = 0;
    private long createTime = 0;
    private long connectionTime = 0;
    private long sendTime = 0;
    private long readTime = 0;
    private long parseTime = 0;
    private long time = 0;
    private long startTime = 0;
    private long timeout = 0;
    private List<ProgressObserver> requests;
    private boolean readTimeProgressTick = true;
    private boolean sendTimeProgressTick = true;
    public final static int TICKS = 5;

    public TimeStat() {
        time = System.currentTimeMillis();
        startTime = time;
    }

    public TimeStat(ProgressObserver request) {
        this.requests = new ArrayList<ProgressObserver>();
        requests.add(request);
        time = System.currentTimeMillis();
        startTime = time;
    }

    public <T extends ProgressObserver> TimeStat(List<T> requests) {
        this.requests = new ArrayList<ProgressObserver>(requests);
        time = System.currentTimeMillis();
        startTime = time;
    }

    public void tickTime(int i) {
        switch (i) {
            case 0:
                tickCreateTime();
                break;
            case 1:
                tickConnectionTime();
                break;
            case 2:
                tickSendTime();
                break;
            case 3:
                tickReadTime();
                break;
            case 4:
                tickParseTime();
                tickEndTime();
                break;
        }
    }


    public void tickCacheTime() {
        for (int i = 0; i < TICKS; i++) {
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
        if (sendTimeProgressTick) {
            progressTick();
        }
    }

    public void tickReadTime() {
        readTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
        if (readTimeProgressTick) {
            progressTick();
        }
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
        LoggerImpl.log(text +
                " create(" + (getCreateTime()) + "ms)" +
                " connection(" + getConnectionTime() + "ms)" +
                " timeout(" + getTimeout() + "ms)" +
                " send(" + getSendTime() + "ms)" +
                " read(" + getReadTime() + "ms) parse(" + getParseTime() + "ms)" +
                " all(" + getAllTime() + "ms)");
    }

    private void progressTick() {
        if (requests != null) {
            for (ProgressObserver request : requests) {
                request.progressTick();
            }
        }
    }

    public void progressTick(float progress) {
        if (requests != null) {
            for (ProgressObserver request : requests) {
                request.progressTick(progress);
            }
        }
    }

    public void setReadTimeProgressTick(boolean readTimeProgressTick) {
        this.readTimeProgressTick = readTimeProgressTick;
    }

    public void setSendTimeProgressTick(boolean sendTimeProgressTick) {
        this.sendTimeProgressTick = sendTimeProgressTick;
    }
}
