package com.github.kubatatami.judonetworking.internals.streams;

import com.github.kubatatami.judonetworking.internals.stats.TimeStat;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 09.09.2013
 * Time: 10:07
 * To change this template use File | Settings | File Templates.
 */
public class RequestInputStream extends InputStream {

    private InputStream stream;
    private TimeStat timeStat;
    private int contentSize;

    public RequestInputStream(InputStream stream, TimeStat timeStat, int contentSize) {
        this.stream = stream;
        this.timeStat = timeStat;
        this.contentSize = contentSize;
        if (contentSize != -1) {
            timeStat.setReadTimeProgressTick(false);
        }
    }

    @Override
    public int read() throws IOException {
        int result = stream.read();
        if (contentSize != -1) {
            timeStat.progressTick(1 / (float) contentSize);
        }
        return result;
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        stream.close();
        timeStat.tickReadTime();
    }

    @Override
    public void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        int result = stream.read(buffer, offset, length);
        if (contentSize != -1 && result != -1) {
            timeStat.progressTick((float) result / (float) contentSize);
        }
        return result;
    }

    @Override
    public synchronized void reset() throws IOException {
        stream.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return stream.skip(byteCount);
    }
}
