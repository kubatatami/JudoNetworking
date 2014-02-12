package org.judonetworking.controllers.raw;

import org.judonetworking.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 21.10.2013
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public abstract class RawController extends ProtocolController {

    private String apiKey = null;
    private String apiKeyName = null;

    public void setApiKey(String name, String key) {
        this.apiKeyName = name;
        this.apiKey = key;
    }

    public void setApiKey(String key) {
        this.apiKey = key;
    }


    @Override
    public abstract RequestInfo createRequest(String url, RequestInterface request) throws Exception;

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return parseResponse(request, stream);
    }

    public static RequestResult parseResponse(RequestInterface request, InputStream stream) {
        if (request.getReturnType().equals(String.class)) {
            return new RequestSuccessResult(request.getId(), convertStreamToString(stream));
        } else if (request.getReturnType().equals(InputStream.class)) {
            return new RequestSuccessResult(request.getId(), stream);
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
                return new ErrorResult(request.getId(), e);
            }
        } else {
            return new ErrorResult(request.getId(), new RequestException("RawController handle string, byte array or input stream response only."));
        }
    }

    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public void parseError(int code, String resp) throws Exception {
        if (code == 405) {
            throw new RequestException("Server response: Method Not Allowed. Did you select the correct protocol controller?", new HttpException(resp, code));
        }
    }

    @Override
    public Object getAdditionalRequestData() {
        return new ApiKey(apiKeyName, apiKey);
    }

    public static class ApiKey {
        public String apiKeyName = null;
        public String apiKey = null;


        public ApiKey(String apiKeyName, String apiKey) {
            this.apiKeyName = apiKeyName;
            this.apiKey = apiKey;
        }
    }

}
