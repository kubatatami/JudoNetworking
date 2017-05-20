package com.github.kubatatami.judonetworking.internals.streams.parts;

import com.github.kubatatami.judonetworking.internals.streams.RequestMultipartEntity;
import com.github.kubatatami.judonetworking.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamPartFormData implements RequestMultipartEntity.PartFormData {

    private final String name;

    private final InputStream inputStream;

    private final String mimeType;

    private final String fileName;

    public InputStreamPartFormData(String name, InputStream inputStream, String mimeType, String fileName) {
        this.name = name;
        this.inputStream = inputStream;
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
        FileUtils.copyStreamOrCountBytes(outputStream, inputStream, getSize());
    }

    @Override
    public long getSize() {
        return -1;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
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
