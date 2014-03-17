package com.github.kubatatami.judonetworking.controllers.json.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.kubatatami.judonetworking.ErrorResult;
import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.RequestComparator;
import com.github.kubatatami.judonetworking.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.RequestSuccessResult;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class JsonRpc2Controller extends JsonRpcController {

    protected int autoBatchTime = 0;
    protected boolean batchEnabled = false;
    protected Comparator<RequestInterface> requestComparator = new RequestComparator();

    public JsonRpc2Controller() {
    }

    public JsonRpc2Controller(boolean batchEnabled) {
        this.batchEnabled = batchEnabled;
    }

    public JsonRpc2Controller(boolean batchEnabled, int autoBatchTime) {
        this.batchEnabled = batchEnabled;
        this.autoBatchTime = autoBatchTime;
    }

    @Override
    public int getAutoBatchTime() {
        return autoBatchTime;
    }

    @Override
    protected Object createRequestModel(String name, Object params, Integer id) {
        return new JsonRpcRequestModel2(name, params, id);
    }

    @Override
    public boolean isBatchSupported() {
        return batchEnabled;
    }


    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        try {
            JsonRpcResponseModel2 response;

            try {
                InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");
                response = mapper.readValue(inputStreamReader, JsonRpcResponseModel2.class);
                inputStreamReader.close();
            } catch (JsonProcessingException ex) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
            } catch (IOException ex) {
                throw new ConnectionException(ex);
            }

            if (response == null) {
                throw new ParseException("Empty response.");
            }

            if (response.jsonrpc == null) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller? Maybe your server doesn't support json-com.github.kubatatami.judonetworking.controllers.json.rpc 2.0? Try JsonRpc1Controller.");
            }

            if (response.error != null) {
                throw new ProtocolException(response.error.message, response.error.code);
            }
            Object result = null;
            if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                try {
                    result = mapper.readValue(response.result.traverse(), mapper.getTypeFactory().constructType(request.getReturnType()));
                } catch (JsonProcessingException ex) {
                    throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
                } catch (IOException ex) {
                    throw new ConnectionException(ex);
                }
                if (!request.isAllowEmptyResult() && result == null) {
                    throw new ParseException("Empty result.");
                }
            }
            return new RequestSuccessResult(request.getId(), result);
        } catch (JudoException e) {
            return new ErrorResult(request.getId(), e);
        }
    }

    @Override
    public ProtocolController.RequestInfo createRequests(String url, List<RequestInterface> requests) throws JudoException {
        try {
            int i = 0;
            ProtocolController.RequestInfo requestInfo = new ProtocolController.RequestInfo();
            requestInfo.url = url;
            Object[] requestsJson = new Object[requests.size()];
            for (RequestInterface request : requests) {
                requestsJson[i] = createRequestObject(request);
                i++;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            mapper.writeValue(writer, requestsJson);
            writer.close();
            requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(stream.toByteArray()), stream.size());
            requestInfo.mimeType = "application/json";
            return requestInfo;
        } catch (IOException ex) {
            throw new JudoException("Can't create request", ex);
        }
    }

    @Override
    public List<RequestResult> parseResponses(List<RequestInterface> requests, InputStream stream, Map<String, List<String>> headers) throws JudoException {
        try {
            List<JsonRpcResponseModel2> responses = null;
            InputStreamReader inputStreamReader = new InputStreamReader(stream, "UTF-8");

            try {
                responses = mapper.readValue(inputStreamReader, mapper.getTypeFactory().constructCollectionType(List.class, JsonRpcResponseModel2.class));

            } catch (JsonProcessingException ex) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller? Maybe your server doesn't support batch? Try JsonRpc2Controller.", ex);
            }

            inputStreamReader.close();


            if (responses == null) {
                throw new ParseException("Empty server response.");
            }

            Collections.sort(responses);
            Collections.sort(requests, requestComparator);

            List<RequestResult> finalResponses = new ArrayList<RequestResult>(responses.size());


            for (int i = 0; i < responses.size(); i++) {
                JsonRpcResponseModel2 res = responses.get(i);
                if (res.jsonrpc == null) {
                    throw new ParseException("Wrong server response. Did you select the correct protocol controller? Maybe your server doesn't support json-com.github.kubatatami.judonetworking.controllers.json.rpc 2.0? Try JsonRpc1Controller.");
                } else if (res.error == null) {
                    Object result = null;
                    try {
                        Type type = requests.get(i).getReturnType();
                        if (!type.equals(Void.class)) {
                            result = mapper.readValue(res.result.traverse(), mapper.getTypeFactory().constructType(requests.get(i).getReturnType()));
                            if (!requests.get(i).isAllowEmptyResult() && result == null) {
                                finalResponses.add(new ErrorResult(requests.get(i).getId(), new ParseException("Empty response.")));
                            } else {
                                finalResponses.add(new RequestSuccessResult(res.id, result));
                            }
                        } else {
                            finalResponses.add(new RequestSuccessResult(res.id, null));
                        }
                    } catch (JsonProcessingException ex) {
                        finalResponses.add(new ErrorResult(res.id, new ParseException(requests.get(i).getName(), ex)));
                    }

                } else {
                    finalResponses.add(new ErrorResult(res.id, new ProtocolException(requests.get(i).getName() + ": " + res.error.message, res.error.code)));
                }
            }
            return finalResponses;
        } catch (IOException ex) {
            throw new ConnectionException(ex);
        }

    }

    protected static class JsonRpcRequestModel2 extends JsonRpcRequestModel {
        final String jsonrpc = "2.0";

        public JsonRpcRequestModel2(String method, Object params, Integer id) {
            super(method, params, id);
        }
    }

    protected static class JsonRpcResponseModel2 extends JsonRpcResponseModel {
        JsonProtocolController.JsonErrorModel error;
        String jsonrpc;
    }


}
