package com.jsonrpclib;

import java.io.Serializable;

public class JsonCacheResult implements Serializable {
    public Object object;
    public boolean result;
    public Long time;
    public String hash;

    public JsonCacheResult() {

    }

    public JsonCacheResult(Object object, boolean result, Long time, String hash) {
        this.object = object;
        this.result = result;
        this.time = time;
        this.hash = hash;
    }
}