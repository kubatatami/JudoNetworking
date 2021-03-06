package com.github.kubatatami.judonetworking.controllers.json.base;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.controllers.json.module.BooleanModule;
import com.github.kubatatami.judonetworking.controllers.json.module.EnumAnnotationModule;

import java.io.Serializable;


/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 11:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class JsonProtocolController extends ProtocolController {
    protected ObjectMapper mapper;
    protected JsonFactory factory;

    protected JsonProtocolController() {
        mapper = getMapperInstance();
        factory = mapper.getFactory();
    }

    public static ObjectMapper getMapperInstance() {
        return getMapperInstance(null);
    }

    public static ObjectMapper getMapperInstance(JsonFactory factory) {
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);

        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new EnumAnnotationModule());
        mapper.registerModule(new BooleanModule());

        return mapper;
    }

    public static class JsonResponseModel implements Serializable {

        private static final long serialVersionUID = 6605507238608079630L;
        public JsonNode result;
    }

    public static class JsonErrorModel implements Serializable {

        private static final long serialVersionUID = -6224009858364300981L;
        public String message;
        public int code;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

}
