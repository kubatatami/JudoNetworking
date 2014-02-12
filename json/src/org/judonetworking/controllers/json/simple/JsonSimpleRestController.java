package org.judonetworking.controllers.json.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.judonetworking.RequestInterface;
import org.judonetworking.RequestResult;
import org.judonetworking.controllers.json.JsonProtocolController;
import org.judonetworking.controllers.raw.RawRestController;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 16.09.2013
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */
public class JsonSimpleRestController extends RawRestController {

    protected ObjectMapper mapper;

    public JsonSimpleRestController() {
        mapper = JsonProtocolController.getMapperInstance();
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override
    public RequestResult parseResponse(RequestInterface request, InputStream stream, Map<String, List<String>> headers) {
        return JsonSimpleController.parseResponse(mapper, request, stream);
    }
}
