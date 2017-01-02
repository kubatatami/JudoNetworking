package com.github.kubatatami.judonetworking.internals.streams.parts;

import android.webkit.MimeTypeMap;

import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.streams.RequestMultipartEntity;
import com.github.kubatatami.judonetworking.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class FilePartFormData implements RequestMultipartEntity.PartFormData {

    private final String name;

    private final File file;

    private final FileInputStream inputStream;

    public FilePartFormData(String name, File file) {
        this.name = name;
        this.file = file;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new JudoException("File is not exist.", e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void write(StringBuilder sb) {
        sb.append("[Binary file size: ");
        sb.append(getSize());
        sb.append("]");
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        FileUtils.copyStream(outputStream, inputStream, getSize());
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public String getMimeType() {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileUtils.getFileExtension(file));
    }

    @Override
    public String getFileName() {
        return file.getName();
    }
}
