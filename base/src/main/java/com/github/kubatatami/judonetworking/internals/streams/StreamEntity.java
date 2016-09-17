package com.github.kubatatami.judonetworking.internals.streams;

import java.io.IOException;
import java.io.OutputStream;

public interface StreamEntity {

    long getContentLength() throws IOException;

    void writeTo(final OutputStream outstream) throws IOException;

    void close() throws IOException;

    String getLog() throws IOException;
}
