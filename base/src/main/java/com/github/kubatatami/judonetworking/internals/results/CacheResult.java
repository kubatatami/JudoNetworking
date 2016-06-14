package com.github.kubatatami.judonetworking.internals.results;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CacheResult implements Serializable {

    private static final long serialVersionUID = -2389625741520157982L;

    public Object object;

    public boolean result;

    public Long time;

    public Map<String, List<String>> headers;

    public CacheResult() {

    }

    public CacheResult(Object object, boolean result, Long time,  Map<String, List<String>> headers) {
        this.object = object;
        this.result = result;
        this.time = time;
        this.headers = headers;
    }
}