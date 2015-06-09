package com.github.kubatatami.judonetworking.controllers.json.rpc;

import android.util.SparseArray;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;
import com.github.kubatatami.judonetworking.internals.results.ErrorResult;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.internals.results.RequestSuccessResult;
import com.github.kubatatami.judonetworking.internals.streams.RequestInputStreamEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
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
    protected Map<Type, JavaType> typeCache = new HashMap<>();

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

    protected JavaType getType(Type type) {
        JavaType javaType = typeCache.get(type);
        if (javaType == null) {
            javaType = mapper.getTypeFactory().constructType(type);
            typeCache.put(type, javaType);
        }
        return javaType;
    }

    protected JsonRpcResponseModel2 readObject(ObjectReader reader, JsonParser parser, Type type, SparseArray<Request> requestMap) throws IOException {
        JsonRpcResponseModel2 responseModel = new JsonRpcResponseModel2();
        JsonNode result = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {

            String fieldName = parser.getCurrentName();
            if (fieldName != null) {
                switch (fieldName) {
                    case "jsonrpc":
                        parser.nextToken();
                        responseModel.jsonrpc = parser.getText();
                        break;
                    case "id":
                        parser.nextToken();
                        responseModel.id = parser.getIntValue();
                        if (requestMap != null) {
                            type = requestMap.get(responseModel.id).getReturnType();
                            if (result != null) {
                                try {
                                    responseModel.result = reader.readValue(result.traverse(), getType(type));
                                } catch (JsonProcessingException ex) {
                                    responseModel.ex = ex;
                                }
                            }
                        }
                        break;
                    case "result":
                        parser.nextToken();

                        result = parser.readValueAsTree();
                        if (type != null && !type.equals(Void.TYPE) && !type.equals(Void.class)) {
                            try {
                                responseModel.result = reader.readValue(result.traverse(), getType(type));
                            } catch (JsonProcessingException ex) {
                                responseModel.ex = ex;
                            }
                        }
                        break;
                    case "error":
                        responseModel.error = new JsonErrorModel();
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            fieldName = parser.getCurrentName();
                            if ("message".equals(fieldName)) {
                                parser.nextToken();
                                responseModel.error.message = parser.getText();
                            } else if ("code".equals(fieldName)) {
                                parser.nextToken();
                                responseModel.error.code = parser.getValueAsInt();
                            }
                        }
                        break;
                }
            }
        }
        return responseModel;
    }


    @Override
    public RequestResult parseResponse(Request request, InputStream stream, Map<String, List<String>> headers) {
        JsonParser parser = null;
        try {
            JsonRpcResponseModel2 response;
            ObjectReader reader = mapper.reader();


            try {
                parser = factory.createParser(stream);
                response = readObject(reader, parser, request.getReturnType(), null);
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
            if (response.ex != null) {
                throw new ParseException(response.ex);
            }
            Object result = null;
            if (!request.isVoidResult()) {
                result = response.result;
                if (!request.isAllowEmptyResult() && result == null) {
                    throw new ParseException("Empty result.");
                }
            }
            return new RequestSuccessResult(request.getId(), result);
        } catch (JudoException e) {
            return new ErrorResult(request.getId(), e);
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ProtocolController.RequestInfo createRequests(String url, List<Request> requests) throws JudoException {
        try {
            int i = 0;
            ProtocolController.RequestInfo requestInfo = new ProtocolController.RequestInfo();
            requestInfo.url = url;
            Object[] requestsJson = new Object[requests.size()];
            for (Request request : requests) {
                requestsJson[i] = createRequestObject(request);
                i++;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mapper.writeValue(stream, requestsJson);
            requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(stream.toByteArray()), stream.size());
            requestInfo.mimeType = "application/json";
            return requestInfo;
        } catch (IOException ex) {
            throw new JudoException("Can't create request", ex);
        }
    }

    @Override
    public List<RequestResult> parseResponses(List<Request> requests, InputStream stream, Map<String, List<String>> headers) throws JudoException {
        JsonParser parser = null;
        try {
            ObjectReader reader = mapper.reader();
            List<RequestResult> finalResponses = new ArrayList<>(requests.size());
            SparseArray<Request> requestMap = new SparseArray<>(requests.size());
            for (Request requestInterface : requests) {
                requestMap.put(requestInterface.getId(), requestInterface);
            }
            parser = factory.createParser(stream);
            parser.nextToken();
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                JsonRpcResponseModel2 res = readObject(reader, parser, null, requestMap);
                Request request = requestMap.get(res.id);
                if (res.jsonrpc == null) {
                    throw new ParseException("Wrong server response. Did you select the correct protocol controller? Maybe your server doesn't support json-rpc 2.0? Try JsonRpc1Controller.");
                } else if (res.ex != null) {
                    finalResponses.add(new ErrorResult(res.id, new ParseException(request.getName(), res.ex)));
                } else if (res.error == null) {
                    Object result;
                    if (!request.isVoidResult()) {
                        result = res.result;
                        if (!request.isAllowEmptyResult() && result == null) {
                            finalResponses.add(new ErrorResult(request.getId(), new ParseException("Empty response.")));
                        } else {
                            finalResponses.add(new RequestSuccessResult(res.id, result));
                        }
                    } else {
                        finalResponses.add(new RequestSuccessResult(res.id, null));
                    }

                } else {
                    finalResponses.add(new ErrorResult(res.id, new ProtocolException(request.getName() + ": " + res.error.message, res.error.code)));
                }

            }
            return finalResponses;
        } catch (JsonProcessingException ex) {
            throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
        } catch (IOException ex) {
            throw new ConnectionException(ex);
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected static class JsonRpcRequestModel2 extends JsonRpcRequestModel {
        final String jsonrpc = "2.0";

        public JsonRpcRequestModel2(String method, Object params, Integer id) {
            super(method, params, id);
        }
    }

    protected static class JsonRpcResponseModel2 extends JsonRpcResponseModel {
        JsonErrorModel error;
        String jsonrpc;
        JsonProcessingException ex;
    }


}
