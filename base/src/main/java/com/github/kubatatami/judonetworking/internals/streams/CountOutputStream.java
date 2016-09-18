package com.github.kubatatami.judonetworking.internals.streams;

import java.io.IOException;
import java.io.OutputStream;


public class CountOutputStream extends OutputStream {

    private int count = 0;

    @Override
    public void write(byte[] b) throws IOException {
        count += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        count += len;
    }

    @Override
    public void write(int b) throws IOException {
        count++;
    }

    public int getCount() {
        return count;
    }
}
