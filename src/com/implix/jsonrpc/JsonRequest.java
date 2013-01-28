package com.implix.jsonrpc;

import java.lang.reflect.Type;

class JsonRequest implements Runnable {
    private Integer id;
    private JsonRpcImplementation rpc;
    private JsonCallback<Object> callback;
    private String name;
    private String[] params;
    private Object[] args;
    private Type type;
    private Integer timeout;
    private String apiKey;

    JsonRequest(Integer id, JsonRpcImplementation rpc, JsonCallback<Object> callback, String name, String[] params, Object[] args, Type type, Integer timeout, String apiKey) {
        this.id = id;
        this.rpc = rpc;
        this.callback = callback;
        this.name = name;
        this.params = params;
        this.args = args;
        this.type = type;
        this.timeout = timeout;
        this.apiKey = apiKey;
    }

    @Override
    public void run() {
        try {
            Object result = rpc.getConnection().call(id, name, params, args, type, timeout, apiKey);
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

    public static void invokeTransactionCallback(JsonRpcImplementation rpc, JsonTransactionCallback transationCallback, Exception e)
    {
        rpc.getHandler().post(new AsyncResult(transationCallback, e));
    }

    public static void invokeTransactionCallback(JsonRpcImplementation rpc, JsonTransactionCallback transationCallback, Object[] results)
    {
        rpc.getHandler().post(new AsyncResult(transationCallback, results));
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
}