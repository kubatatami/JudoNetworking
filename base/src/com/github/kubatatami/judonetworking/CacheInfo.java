package com.github.kubatatami.judonetworking;

import java.io.Serializable;

/**
 * Created by Kuba on 28/05/14.
 */
public class CacheInfo implements Serializable{
    public boolean isCached;
    public Long dataTime;

    public CacheInfo() {
    }

    public CacheInfo(boolean isCached, Long dataTime) {
        this.isCached = isCached;
        this.dataTime = dataTime;
    }
}
