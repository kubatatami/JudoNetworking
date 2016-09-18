package com.github.kubatatami.judonetworking.internals.streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

public class RequestMultipartEntity implements StreamEntity {

    private static final byte[] CRLF = {'\r', '\n'};

    public static final String BOUNDARY = UUID.randomUUID().toString().replaceAll("-", "");

    private static Charset charset = Charset.forName("UTF-8");

    private List<PartFormData> parts;

    public RequestMultipartEntity(List<PartFormData> parts) {
        this.parts = parts;
    }

    @Override
    public long getContentLength() throws IOException {
        CountOutputStream outputStream = new CountOutputStream();
        write(null, outputStream);
        return outputStream.getCount();
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
            write(sb, outStream, "Content-Disposition: form-data; name=\"" + part.getName() + "\"");
            if (part.getFileName() != null && !part.getFileName().isEmpty()) {
                write(sb, outStream, "; filename=\"" + part.getFileName() + "\"");
            }
            writeNewLine(sb, outStream);
            if (part.getMimeType() != null && !part.getMimeType().isEmpty()) {
                writeLine(sb, outStream, "Content-Type: " + part.getMimeType());
            }
            if (part.getSize() >= 0) {
                writeLine(sb, outStream, "Content-Length: " + part.getSize());
            }
            writeNewLine(sb, outStream);
            write(sb, outStream, part);
            writeNewLine(sb, outStream);
        }
        write(sb, outStream, "--");
        write(sb, outStream, BOUNDARY);
        write(sb, outStream, "--");
        writeNewLine(sb, outStream);
    }

    private void write(StringBuilder sb, OutputStream outStream, PartFormData part) throws IOException {
        if (sb != null) {
            part.write(sb);
        }
        if (outStream != null) {
            part.write(outStream);
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
            part.close();
        }
    }

    public static String getMimeType() {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    public interface PartFormData {

        String getName();

        void write(StringBuilder sb);

        void write(OutputStream outputStream) throws IOException;

        long getSize();

        void close() throws IOException;

        String getMimeType();

        String getFileName();
    }
}
