package com.github.kubatatami.judonetworking;

import java.io.Serializable;

/**
 * Created by Kuba on 28/05/14.
 */
public class CacheInfo implements Serializable {

    private static final long serialVersionUID = -5445644584008032363L;

    public boolean isCached;

    public Long dataTime;

    public CacheInfo() {
    }

    public CacheInfo(boolean isCached, Long dataTime) {
        this.isCached = isCached;
        this.dataTime = dataTime;
    }
}
