package com.jsonrpclib;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 09.09.2013
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */
public class JsonOutputStream extends OutputStream {
    private OutputStream stream;
    private JsonTimeStat timeStat;

    public JsonOutputStream(OutputStream stream, JsonTimeStat timeStat) {
        this.stream = stream;
        this.timeStat = timeStat;
    }

    @Override
    public void write(int oneByte) throws IOException {
        stream.write(oneByte);
    }

    @Override
    public void close() throws IOException {
        stream.close();
        timeStat.tickSendTime();
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        stream.write(buffer);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        stream.write(buffer, offset, count);
    }
}
