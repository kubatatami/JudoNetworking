package com.github.kubatatami.judonetworking.utils;

import com.github.kubatatami.judonetworking.internals.streams.CountOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class FileUtils {

    private final static long BUFFER_SIZE = 4096;

    public static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static void copyStream(OutputStream outstream, InputStream instream, long length) throws IOException {
        int size = (int) (length > 0 ? Math.min(BUFFER_SIZE, length) : BUFFER_SIZE);
        byte[] buffer = new byte[size];
        int l;
        if (length < 0) {
            // consume until EOF
            while ((l = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, l);
            }
        } else {
            // consume no more than length
            long remaining = length;
            while (remaining > 0) {
                l = instream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (l == -1) {
                    break;
                }
                outstream.write(buffer, 0, l);
                remaining -= l;
            }
        }
        outstream.flush();
    }

    public static void copyStreamOrCountBytes(OutputStream outputStream, InputStream inputStream, long length) throws IOException {
        if (outputStream instanceof CountOutputStream && length > 0) {
            ((CountOutputStream) outputStream).addBytes(length);
        } else {
            copyStream(outputStream, inputStream, length);
        }
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
