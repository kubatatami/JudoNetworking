package com.github.kubatatami.judonetworking.internals.streams;

import com.github.kubatatami.judonetworking.utils.FileUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class RequestMultipartEntity implements StreamEntity {

    private final static int BUFFER_SIZE = 4096;

    private final static String NEW_LINE = "\r\n";

    public static final String BOUNDARY = "-----------------------------735323031399963166993862150";

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
        byte[] buffer = new byte[BUFFER_SIZE];
        DataOutputStream data = new DataOutputStream(outstream);
        for (PartFormData part : parts) {
            data.writeBytes(BOUNDARY);
            data.writeBytes(NEW_LINE);
            data.writeBytes("Content-Disposition: form-data; name=\"" + part.getName() +"\"; " + "filename=\"" + part.getFileName() +"\"" + NEW_LINE);
            if (part.mimeType != null && !part.getMimeType().isEmpty()) {
                data.writeBytes("Content-Type: " + part.getMimeType() + NEW_LINE);
            }
            data.writeBytes(NEW_LINE);
            data.flush();
            FileUtils.copyStream(outstream, part.getInputStream(), -1, buffer);
            outstream.flush();
            data.writeBytes(NEW_LINE);
            data.flush();
        }
        data.writeBytes(BOUNDARY);
        data.writeBytes("--");
        data.flush();
        outstream.close();
    }

    @Override
    public void close() throws IOException {
        for (PartFormData part : parts) {
            part.getInputStream().close();
        }
    }

    @Override
    public String getLog() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (PartFormData part : parts) {
            sb.append(BOUNDARY);
            sb.append("\n");
            sb.append("Content-Disposition: form-data; name=\"" + part.getName() +"\"; " + "filename=\"" + part.getFileName() +"\"" + "\n");
            if (part.mimeType != null && !part.getMimeType().isEmpty()) {
                sb.append("Content-Type: " + part.getMimeType() + "\n");
            }
            sb.append("\n");
            sb.append("Binary body");
            sb.append("\n");
        }
        sb.append(BOUNDARY);
        sb.append("--");
        return sb.toString();
    }

    public static String getMimeType() {
        return "multipart/form-data;boundary=" + BOUNDARY;
    }

    public static class PartFormData {

        private String name;

        private InputStream inputStream;

        private String mimeType;

        private String fileName;

        public PartFormData(String name, InputStream inputStream, String mimeType, String fileName) {
            this.name = name;
            this.inputStream = inputStream;
            this.mimeType = mimeType;
            this.fileName = fileName;
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
    }
}
