package com.jsonrpclib;

class JsonVirtualServerInfo {
    public Object server;
    public int minDelay;
    public int maxDelay;

    JsonVirtualServerInfo(Object server, int minDelay, int maxDelay) {
        this.server = server;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
    }
}