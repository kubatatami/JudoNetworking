package com.github.kubatatami.judonetworking.controllers.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class BooleanModule extends SimpleModule {


    public BooleanModule() {
        super("boolean-module", new Version(1, 0, 0, "", BooleanModule.class.getPackage().getName(), ""));
        addDeserializer(Boolean.class, new StdBooleanDeserializer());
        addDeserializer(Boolean.TYPE, new StdBooleanDeserializer());
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
    }

    public static class StdBooleanDeserializer extends JsonDeserializer<Boolean> {

        @Override
        public Boolean deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_TRUE) {
                return true;
            }
            if (t == JsonToken.VALUE_FALSE) {
                return false;
            }
            if (t == JsonToken.VALUE_NULL) {
                return null;
            }
            if (t == JsonToken.VALUE_NUMBER_INT) {
                return (jp.getIntValue() != 0);
            }
            if (t == JsonToken.VALUE_STRING) {
                String text = jp.getText().trim();
                if ("true".equals(text)) {
                    return true;
                }
                if ("false".equals(text)) {
                    return Boolean.FALSE;
                }

                if ("n".equals(text)) {
                    return Boolean.FALSE;
                }

                if ("y".equals(text)) {
                    return Boolean.TRUE;
                }

                if ("no".equals(text)) {
                    return Boolean.FALSE;
                }

                if ("yes".equals(text)) {
                    return Boolean.TRUE;
                }

                if ("0".equals(text)) {
                    return Boolean.FALSE;
                }

                if ("1".equals(text)) {
                    return Boolean.TRUE;
                }
            }
            throw ctxt.mappingException(Boolean.class);
        }

    }

}