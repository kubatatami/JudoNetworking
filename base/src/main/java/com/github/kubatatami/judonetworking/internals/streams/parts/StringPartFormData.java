package com.github.kubatatami.judonetworking.internals.streams.parts;

import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.streams.RequestMultipartEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class StringPartFormData implements RequestMultipartEntity.PartFormData {

    private final String name;

    private final String value;

    private final String mimeType;

    private final String fileName;

    private final byte[] data;

    public StringPartFormData(String name, String value, String mimeType, String fileName) {
        this.name = name;
        this.value = value;
        this.mimeType = mimeType;
        this.fileName = fileName;
        try {
            this.data = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new JudoException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void write(StringBuilder sb) {
        sb.append(value);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(data);
    }

    @Override
    public long getSize() {
        return -1;
    }

    @Override
    public void close() {

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
