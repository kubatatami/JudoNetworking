package com.jsonrpclib;

import android.util.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JsonRequest implements Runnable, Comparable<JsonRequest> {
    private Integer id;
    private final JsonRpcImplementation rpc;
    private JsonCallbackInterface<Object> callback;
    private final String name;
    private final String[] params;
    private final Object[] args;
    private Type returnType;
    private final int timeout;
    private final String apiKey;
    private boolean cachable;
    private boolean highPriority;
    private int cacheLifeTime;
    private int cacheSize;
    private JsonMethodType methodType;

    public JsonRequest(String name, JsonRpcImplementation rpc, String[] params, Object[] args, int timeout, String apiKey) {
        this.name = name;
        this.rpc = rpc;
        this.params = params;
        this.args = args;
        this.timeout = timeout;
        this.apiKey = apiKey;
    }

    public JsonRequest(Integer id, JsonRpcImplementation rpc, JsonCallbackInterface<Object> callback, String name, String[] params,
                Object[] args, Type returnType, int timeout, String apiKey, boolean cachable,
                int cacheLifeTime,int cacheSize,JsonMethodType methodType, boolean highPriority) {
        this.id = id;
        this.rpc = rpc;
        this.callback = callback;
        this.name = name;
        this.params = params;
        this.args = args;
        this.returnType = returnType;
        this.timeout = timeout;
        this.apiKey = apiKey;
        this.cachable = cachable;
        this.cacheLifeTime = cacheLifeTime;
        this.cacheSize = cacheSize;
        this.methodType = methodType;
        this.highPriority=highPriority;
    }

    public JsonRequest(Integer id, JsonRpcImplementation rpc, String name, String[] params,
                       Object[] args, Type returnType, int timeout, String apiKey, boolean cachable,
                       int cacheLifeTime,int cacheSize,JsonMethodType methodType) {
        this.id = id;
        this.rpc = rpc;
        this.name = name;
        this.params = params;
        this.args = args;
        this.returnType = returnType;
        this.timeout = timeout;
        this.apiKey = apiKey;
        this.cachable = cachable;
        this.cacheLifeTime = cacheLifeTime;
        this.cacheSize = cacheSize;
        this.methodType = methodType;
    }

    public JsonRequestModel createJsonRequest(JsonRpcVersion version) {
        Object finalArgs;
        if (args != null && rpc.isByteArrayAsBase64()) {
            int i = 0;
            for (Object object : args) {
                if (object instanceof byte[]) {
                    args[i] = Base64.encodeToString((byte[]) object, Base64.NO_WRAP);
                }
                i++;
            }
        }

        if (params != null && args != null) {
            int i = 0;
            Map<String, Object> paramObjects = new HashMap<String, Object>();
            for (String param : params) {
                paramObjects.put(param, args[i]);
                i++;
            }
            finalArgs = paramObjects;
            if (apiKey != null) {
                finalArgs = new Object[]{apiKey, finalArgs};
            }
        } else {
            finalArgs = args;
            if (apiKey != null) {
                if (args != null) {
                    Object[] finalArray = new Object[args.length + 1];
                    finalArray[0] = apiKey;
                    System.arraycopy(args, 0, finalArray, 1, args.length);
                    finalArgs = finalArray;
                } else {
                    finalArgs = new Object[]{apiKey};
                }
            }
        }
        if (version == JsonRpcVersion.VERSION_1_0_NO_ID) {
            return new JsonRequestModel(name, finalArgs, null);
        } else if (version == JsonRpcVersion.VERSION_1_0) {
            return new JsonRequestModel(name, finalArgs, id);
        } else {
            return new JsonRequestModel2(name, finalArgs, id);
        }


    }


    public String createGetRequest() {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        int i = 0;
        if (apiKey != null && params.length - 1 == args.length) {
            nameValuePairs.add(new BasicNameValuePair(params[0], apiKey));
            i++;
        }

        for (Object arg : args) {
            nameValuePairs.add(new BasicNameValuePair(params[i], arg==null ? "" : arg.toString()));
            i++;
        }


        return (name + "?" + URLEncodedUtils.format(nameValuePairs, HTTP.UTF_8)).replaceAll("\\+", "%20");
    }

    @Override
    public void run() {
        try {
            Object result = rpc.getJsonConnector().call(this);
            invokeCallback(result);
        } catch (Exception e) {
            invokeCallback(e);
        }
    }

    public void invokeCallback(Exception e)
    {
        if(callback!=null)
        {
            rpc.getHandler().post(new JsonAsyncResult(callback, e));
        }
    }

    public void invokeCallback(Object result)
    {
        if(callback!=null)
        {
            rpc.getHandler().post(new JsonAsyncResult(callback, result));
        }
    }

    public static void invokeBatchCallback(JsonRpcImplementation rpc, JsonBatch<?> batch, Exception e)
    {
        rpc.getHandler().post(new JsonAsyncResult(batch, e));
    }

    public static void invokeBatchCallback(JsonRpcImplementation rpc, JsonBatch<?> batch, Object[] results)
    {
        rpc.getHandler().post(new JsonAsyncResult(batch, results));
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Object[] getArgs() {
        return args;
    }

    public Type getReturnType() {
        return returnType;
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
            return Math.max(rpc.getStats().get(name).avgTime,1);
        }
        else
        {
            return timeout/2;
        }
    }

    @Override
    public int compareTo(JsonRequest another) {
        if(highPriority && !another.highPriority)
        {
            return -1;
        }
        else if(!highPriority && another.highPriority)
        {
            return 1;
        }
        else
        {
            return Long.valueOf(another.getWeight()).compareTo(getWeight());
        }
    }

    public JsonMethodType getMethodType() {
        return methodType;
    }

    boolean isHighPriority() {
        return highPriority;
    }
}