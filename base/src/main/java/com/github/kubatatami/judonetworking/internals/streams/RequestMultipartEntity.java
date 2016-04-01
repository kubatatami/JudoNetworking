package com.github.kubatatami.judonetworking.internals.streams;

import com.github.kubatatami.judonetworking.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

public class RequestMultipartEntity implements StreamEntity {

    private static final byte[] CRLF = {'\r', '\n'};

    public static final String BOUNDARY = UUID.randomUUID().toString();

    private static Charset charset = Charset.forName("UTF-8");

    private List<PartFormData> parts;

    public RequestMultipartEntity(List<PartFormData> parts) {
        this.parts = parts;
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        write(null, outstream);
    }

    @Override
    public String getLog() throws IOException {
        StringBuilder sb = new StringBuilder();
        write(sb, null);
        return sb.toString();
    }

    private void write(StringBuilder sb, OutputStream outStream) throws IOException {
        for (PartFormData part : parts) {
            write(sb, outStream, "--");
            writeLine(sb, outStream, BOUNDARY);
            writeLine(sb, outStream, "Content-Disposition: form-data; name=\"" + part.getName() + "\"; filename=\"" + part.getFileName() + "\"");
            if (part.getMimeType() != null && !part.getMimeType().isEmpty()) {
                writeLine(sb, outStream, "Content-Type: " + part.getMimeType());
            }
            if (part.getSize() >= 0) {
                writeLine(sb, outStream, "Content-Length: " + part.getSize());
            }
            writeNewLine(sb, outStream);
            write(sb, outStream, part.getInputStream(), part.getSize());
            writeNewLine(sb, outStream);
        }
        write(sb, outStream, "--");
        write(sb, outStream, BOUNDARY);
        write(sb, outStream, "--");
        writeNewLine(sb, outStream);
    }

    private void write(StringBuilder sb, OutputStream outStream, InputStream inputStream, long size) throws IOException {
        if (sb != null) {
            sb.append("[Binary body size: ");
            sb.append(size);
            sb.append("]");
        }
        if (outStream != null) {
            FileUtils.copyStream(outStream, inputStream, size);
        }
    }

    private void writeLine(StringBuilder sb, OutputStream outStream, String line) throws IOException {
        if (sb != null) {
            sb.append(line);
        }
        if (outStream != null) {
            outStream.write(line.getBytes(charset));
        }
        writeNewLine(sb, outStream);
    }

    private void write(StringBuilder sb, OutputStream outStream, String line) throws IOException {
        if (sb != null) {
            sb.append(line);
        }
        if (outStream != null) {
            outStream.write(line.getBytes(charset));
        }
    }

    private void writeNewLine(StringBuilder sb, OutputStream outStream) throws IOException {
        if (sb != null) {
            sb.append('\n');
        }
        if (outStream != null) {
            outStream.write(CRLF);
        }
    }

    @Override
    public void close() throws IOException {
        for (PartFormData part : parts) {
            part.getInputStream().close();
        }
    }

    public static String getMimeType() {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    public static class PartFormData {

        private String name;

        private InputStream inputStream;

        private String mimeType;

        private String fileName;

        private final long size;

        public PartFormData(String name, InputStream inputStream, String mimeType, String fileName, long size) {
            this.name = name;
            this.inputStream = inputStream;
            this.mimeType = mimeType;
            this.fileName = fileName;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getFileName() {
            return fileName;
        }

        public long getSize() {
            return size;
        }
    }
}
