package com.github.kubatatami.judonetworking.controllers.json.rpc;

import android.util.SparseArray;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.kubatatami.judonetworking.ErrorResult;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.RequestResult;
import com.github.kubatatami.judonetworking.RequestSuccessResult;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.ParseException;
import com.github.kubatatami.judonetworking.exceptions.ProtocolException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 23.07.2013
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class JsonRpc1Controller extends JsonRpcController {


    @Override
    protected Object createRequestModel(String name, Object params, Integer id) {
        return new JsonRpcRequestModel(name, params, id);
    }

    protected JsonRpcResponseModel1 readObject(ObjectReader reader, JsonParser parser, Type type) throws IOException {
        JsonRpcResponseModel1 responseModel = new JsonRpcResponseModel1();
        while (parser.nextToken() != JsonToken.END_OBJECT) {

            String fieldname = parser.getCurrentName();
            if ("id".equals(fieldname)) {
                parser.nextToken();
                responseModel.id=parser.getIntValue();
            }else if ("result".equals(fieldname)){
                parser.nextToken();
                responseModel.result=reader.readValue(parser,mapper.getTypeFactory().constructType(type));
            }else if ("error".equals(fieldname)){
                parser.nextToken();
                responseModel.error=parser.getText();
            }
        }
        return responseModel;
    }


        @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        JsonParser parser = null;
        ObjectReader reader = mapper.reader();
        try {
            JsonRpcResponseModel1 response;
            try {
                parser = factory.createParser(stream);
                response = readObject(reader,parser,request.getReturnType());

                if (response == null) {
                    throw new ParseException("Empty server response.");
                }
                if (response.error != null) {
                    throw new ProtocolException(response.error);
                }

                if (!request.getReturnType().equals(Void.TYPE) && !request.getReturnType().equals(Void.class)) {
                    Object result;

                    result = response.result;

                    if (!request.isAllowEmptyResult() && result == null) {
                        throw new ParseException("Empty result.");
                    }
                    return new RequestSuccessResult(request.getId(), result);
                }
            } catch (JsonProcessingException ex) {
                throw new ParseException("Wrong server response. Did you select the correct protocol controller?", ex);
            } catch (IOException ex) {
                throw new ConnectionException(ex);
            }
            return new RequestSuccessResult(request.getId(), null);
        } catch (JudoException e) {
            return new ErrorResult(request.getId(), e);
        }finally {
            if(parser!=null){
                try {
                    parser.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    protected class JsonRpcResponseModel1 extends JsonRpcResponseModel {
        String error;
    }

}
