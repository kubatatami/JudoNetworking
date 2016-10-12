package com.github.kubatatami.judonetworking.internals.streams.parts;

import com.github.kubatatami.judonetworking.internals.streams.RequestMultipartEntity;

import java.io.IOException;
import java.io.OutputStream;

public class BytePartFormData implements RequestMultipartEntity.PartFormData {

    private final String name;

    private final byte[] bytes;

    private final String mimeType;

    private final String fileName;

    public BytePartFormData(String name, byte[] bytes, String mimeType, String fileName) {
        this.name = name;
        this.bytes = bytes;
        this.mimeType = mimeType;
        this.fileName = fileName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void write(StringBuilder sb) {
        sb.append("[Binary input stream]");
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(bytes);
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
