package com.github.kubatatami.judonetworking.internals.virtuals;

public class VirtualServerInfo {

    public Object server;

    public int minDelay;

    public int maxDelay;

    public VirtualServerInfo(Object server, int minDelay, int maxDelay) {
        this.server = server;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
    }
}