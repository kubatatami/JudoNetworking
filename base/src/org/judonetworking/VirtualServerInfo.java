package org.judonetworking;

class VirtualServerInfo {
    public Object server;
    public int minDelay;
    public int maxDelay;

    VirtualServerInfo(Object server, int minDelay, int maxDelay) {
        this.server = server;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
    }
}