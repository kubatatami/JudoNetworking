package com.jsonrpclib;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 30.03.2013
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */
class JsonTimeStat {
    private long allTime = 0;
    private long createTime = 0;
    private long connectionTime = 0;
    private long sendTime = 0;
    private long readTime = 0;
    private long parseTime = 0;
    private long time = 0;
    private long startTime = 0;
    private long timeout = 0;

    public JsonTimeStat()
    {
        time = System.currentTimeMillis();
        startTime = time;
    }

    public void tickCreateTime()
    {
        createTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
    }

    public void tickConnectionTime()
    {
        connectionTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
    }

    public void tickSendTime()
    {
        sendTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
    }

    public void tickReadTime()
    {
        readTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
    }

    public void tickParseTime()
    {
        parseTime = System.currentTimeMillis() - time;
    }

    public void tickEndTime()
    {
        allTime=System.currentTimeMillis() - startTime;
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

    public long getMethodTime()
    {
        return allTime-connectionTime;
    }

    public void logTime(String text)
    {
        JsonLoggerImpl.log(text +
                " create(" + (getCreateTime()) + "ms)" +
                " connection(" + getConnectionTime() + "ms)" +
                " timeout(" + getTimeout() + "ms)" +
                " send(" + getSendTime() + "ms)" +
                " read(" + getReadTime() + "ms) parse(" + getParseTime() + "ms)" +
                " all(" + getAllTime() + "ms)");
    }

}
