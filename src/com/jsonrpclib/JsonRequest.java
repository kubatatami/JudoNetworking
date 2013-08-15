package com.jsonrpclib;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

class JsonRequest implements Runnable, Comparable<JsonRequest>, JsonProgressObserver, JsonRequestInterface {
    private Integer id;
    private final JsonRpcImplementation rpc;
    private JsonCallbackInterface<Object> callback;
    private final String name;
    private final int timeout;
    private JsonMethod ann;
    private int progress = 0;
    private int max = JsonTimeStat.TICKS;
    private final Object[] args;
    private Type returnType;
    private Method method;

    public JsonRequest(Integer id, JsonRpcImplementation rpc, Method method, String name, JsonMethod ann,
                       Object[] args, Type returnType, int timeout, JsonCallbackInterface<Object> callback) {
        this.id = id;
        this.name = name;
        this.timeout = timeout;
        this.method = method;
        this.rpc = rpc;
        this.ann = ann;
        this.args = args;
        this.returnType = returnType;
        this.callback = callback;
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

    public void invokeCallback(Exception e) {
        if (callback != null) {
            rpc.getHandler().post(new JsonAsyncResult(callback, e));
        }
    }

    public void invokeCallback(Object result) {
        if (callback != null) {
            rpc.getHandler().post(new JsonAsyncResult(callback, result));
        }
    }

    public static void invokeBatchCallback(final JsonRpcImplementation rpc, JsonBatch<?> batch, final Exception e) {
        if (rpc.getErrorLogger() != null) {
            rpc.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    rpc.getErrorLogger().onError(e);
                }
            });

        }
        rpc.getHandler().post(new JsonAsyncResult(batch, e));
    }

    public static void invokeBatchCallback(JsonRpcImplementation rpc, JsonBatch<?> batch, Object[] results) {
        rpc.getHandler().post(new JsonAsyncResult(batch, results));
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public String[] getParamNames() {
        return ann.paramNames();
    }

    @Override
    public Method getMethod() {
        return method;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public int getCacheLifeTime() {
        return ann.cacheLifeTime();
    }

    public boolean isCachable() {
        return ann.cacheable();
    }

    public int getCacheSize() {
        return ann.cacheSize();
    }

    public long getWeight() {
        if (rpc.getStats().containsKey(name)) {
            return Math.max(rpc.getStats().get(name).avgTime, 1);
        } else {
            return timeout / 2;
        }
    }

    @Override
    public int compareTo(JsonRequest another) {
        if (ann.highPriority() && !another.isHighPriority()) {
            return -1;
        } else if (!ann.highPriority() && another.isHighPriority()) {
            return 1;
        } else {
            return Long.valueOf(another.getWeight()).compareTo(getWeight());
        }
    }


    public boolean isHighPriority() {
        return ann.highPriority();
    }

    public void progressTick() {
        progress++;
        if (callback != null) {
            rpc.getHandler().post(new JsonAsyncResult(callback, progress * 100 / max));
        }
    }

    @Override
    public void setMaxProgress(int max) {
        this.max = max;
    }

    @Override
    public int getMaxProgress() {
        return max;
    }

    public boolean isCachePersist() {
        return ann.cachePersist();
    }


}