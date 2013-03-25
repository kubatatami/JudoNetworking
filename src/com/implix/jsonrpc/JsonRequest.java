package com.implix.jsonrpc;

import java.lang.reflect.Type;

class JsonRequest implements Runnable, Comparable<JsonRequest> {
    private Integer id;
    private JsonRpcImplementation rpc;
    private JsonCallback<Object> callback;
    private String name;
    private String[] params;
    private Object[] args;
    private Type type;
    private int timeout;
    private String apiKey;
    private boolean cachable;
    private int cacheLifeTime;
    private int cacheSize;

    JsonRequest(Integer id, JsonRpcImplementation rpc, JsonCallback<Object> callback, String name, String[] params, Object[] args, Type type, int timeout, String apiKey, boolean cachable, int cacheLifeTime,int cacheSize) {
        this.id = id;
        this.rpc = rpc;
        this.callback = callback;
        this.name = name;
        this.params = params;
        this.args = args;
        this.type = type;
        this.timeout = timeout;
        this.apiKey = apiKey;
        this.cachable = cachable;
        this.cacheLifeTime = cacheLifeTime;
        this.cacheSize = cacheSize;

    }

    @Override
    public void run() {
        try {
            Object result = rpc.getJsonConnector().call(id, name, params, args, type, timeout, apiKey,cachable,cacheLifeTime, cacheSize);
            invokeCallback(result);
        } catch (Exception e) {
            invokeCallback(e);
        }
    }

    public void invokeCallback(Exception e)
    {
        if(callback!=null)
        {
            rpc.getHandler().post(new AsyncResult(callback, e));
        }
    }

    public void invokeCallback(Object result)
    {
        if(callback!=null)
        {
            rpc.getHandler().post(new AsyncResult(callback, result));
        }
    }

    public static void invokeBatchCallback(JsonRpcImplementation rpc, JsonBatch<?> batch, Exception e)
    {
        rpc.getHandler().post(new AsyncResult(batch, e));
    }

    public static void invokeBatchCallback(JsonRpcImplementation rpc, JsonBatch<?> batch, Object[] results)
    {
        rpc.getHandler().post(new AsyncResult(batch, results));
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String[] getParams() {
        return params;
    }

    public Object[] getArgs() {
        return args;
    }

    public Type getType() {
        return type;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public int getCacheLifeTime() {
        return cacheLifeTime;
    }

    public boolean isCachable() {
        return cachable;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public long getWeight()
    {
        if(rpc.getStats().containsKey(name))
        {
            return rpc.getStats().get(name).avgTime;
        }
        else
        {
            return timeout/2;
        }
    }

    @Override
    public int compareTo(JsonRequest another) {
        return Long.valueOf(another.getWeight()).compareTo(getWeight());  //To change body of implemented methods use File | Settings | File Templates.
    }
}