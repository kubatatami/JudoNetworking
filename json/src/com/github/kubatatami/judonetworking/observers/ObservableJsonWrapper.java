package com.github.kubatatami.judonetworking.observers;

import android.content.Context;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kubatatami.judonetworking.controllers.json.JsonProtocolController;

import java.io.OutputStream;

/**
 * Created by Kuba on 22/04/15.
 */
public class ObservableJsonWrapper<T> extends ObservablePersistentWrapper<T> {

    protected ObjectMapper mapper;
    protected Class<T> type;

    public ObservableJsonWrapper(Context context, String persistentKey, Class<T> type) {
        this(context, persistentKey, type, JsonProtocolController.getMapperInstance());
    }

    public ObservableJsonWrapper(Context context, String persistentKey, Level level, Class<T> type) {
        this(context, persistentKey, type, level, JsonProtocolController.getMapperInstance());
    }

    public ObservableJsonWrapper(Context context, String persistentKey, Class<T> type, ObjectMapper mapper) {
        super(context, persistentKey);
        this.type = type;
        this.mapper = mapper;
    }

    public ObservableJsonWrapper(Context context, String persistentKey, Class<T> type, Level level, ObjectMapper mapper) {
        super(context, persistentKey, level);
        this.type = type;
        this.mapper = mapper;
    }

    @Override
    protected PersistentData<T> loadObject(byte[] array) throws Exception {
        JavaType javaType = mapper.getTypeFactory().constructParametrizedType(PersistentData.class, PersistentData.class, type);
        return mapper.readValue(array, javaType);
    }

    @Override
    protected void saveObject(OutputStream fileStream, PersistentData<T> data) throws Exception {
        mapper.writeValue(fileStream, data);
    }
}
