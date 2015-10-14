package com.github.kubatatami.judonetworking.internals.results;

import java.io.Serializable;

public class CacheResult implements Serializable {

    private static final long serialVersionUID = -2389625741520157982L;

    public Object object;

    public boolean result;

    public Long time;

    public CacheResult() {

    }

    public CacheResult(Object object, boolean result, Long time) {
        this.object = object;
        this.result = result;
        this.time = time;
    }
}