package com.github.kubatatami.judonetworking.controllers.json;

import android.support.v4.util.LruCache;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.Deserializers.Base;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;

import java.io.IOException;
import java.lang.reflect.Field;

public class EnumAnnotationModule extends SimpleModule {

    protected static ObjectMapper mapper = new ObjectMapper();

    public EnumAnnotationModule() {
        super("enum-annotation", new Version(1, 0, 0, "", EnumAnnotationModule.class.getPackage().getName(), ""));
        addSerializer(Enum.class, new EnumAnnotationSerializer());
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        Base deser = new Deserializers.Base() {
            @SuppressWarnings("unchecked")
            @Override
            public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                                                            DeserializationConfig config, BeanDescription beanDesc)
                    throws JsonMappingException {

                return new EnumAnnotationDeserializer((Class<Enum<?>>) type);
            }
        };
        context.addDeserializers(deser);
    }


    public static class EnumAnnotationSerializer extends StdScalarSerializer<Enum> {

        public EnumAnnotationSerializer() {
            super(Enum.class, false);
        }

        @Override
        public void serialize(Enum value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            try {
                JsonProperty property = value.getDeclaringClass().getDeclaredField(value.name()).getAnnotation(JsonProperty.class);
                if (property != null) {
                    jgen.writeString(property.value());
                    return;
                }
            } catch (NoSuchFieldException ignored) {
            }
            mapper.writeValue(jgen, value);
        }
    }

    public static class EnumAnnotationDeserializer extends StdScalarDeserializer<Enum<?>> {

        LruCache<Field, String> propertyValueCache = new LruCache<>(100);

        protected EnumAnnotationDeserializer(Class<Enum<?>> clazz) {
            super(clazz);

        }

        @Override
        public Enum<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            String text = jp.getText();
            Enum<?> defaultEnum = null;
            try {

                for (Field field : ReflectionCache.getDeclaredFields(handledType())) {
                    if (field.isEnumConstant()) {
                        String value = propertyValueCache.get(field);
                        if (value == null) {
                            JsonProperty property = ReflectionCache.getAnnotation(field, JsonProperty.class);
                            if (property != null) {
                                value = property.value();
                                propertyValueCache.put(field, value);
                            }
                        }
                        if (text.equals(value)) {
                            return (Enum<?>) field.get(null);
                        }
                        if (ReflectionCache.getAnnotation(field, JsonDefaultEnum.class) != null) {
                            if (defaultEnum == null) {
                                defaultEnum = (Enum<?>) field.get(null);
                            } else {
                                throw new RuntimeException("It can be only one JsonDefaultEnum");
                            }
                        }
                    }
                }


            } catch (Exception ignored) {
            }
            try {
                return (Enum<?>) mapper.readValue("\"" + text + "\"", handledType());
            } catch (InvalidFormatException e) {
                if (defaultEnum != null) {
                    return defaultEnum;
                } else {
                    throw e;
                }
            }
        }

    }

}