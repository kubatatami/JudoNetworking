package com.github.kubatatami.judonetworking.controllers.raw;

import com.github.kubatatami.judonetworking.ErrorResult;
import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.RequestSuccessResult;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.HttpException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 21.10.2013
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public abstract class RawController extends ProtocolController {


    @Override
    public abstract RequestInfo createRequest(String url, RequestInterface request) throws JudoException;

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return parseResponse(request, stream);
    }

    public static RequestResult parseResponse(RequestInterface request, InputStream stream) {
        if (request.getReturnType().equals(String.class)) {
            return new RequestSuccessResult(request.getId(), convertStreamToString(stream));
        } else if (request.getReturnType().equals(Byte[].class)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];
            try {
                while ((nRead = stream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return new RequestSuccessResult(request.getId(), buffer.toByteArray());
            } catch (Exception e) {
                return new ErrorResult(request.getId(), new ConnectionException(e));
            }
        } else {
            return new ErrorResult(request.getId(), new ParseException("RawController handle string or byte array response only."));
        }
    }


    @Override
    public void parseError(int code, String resp) throws JudoException {
        if (code == 405) {
            throw new ProtocolException("Server response: Method Not Allowed. Did you select the correct protocol controller?", new HttpException(resp, code));
        }
    }


}
