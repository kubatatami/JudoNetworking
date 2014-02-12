package org.judonetworking;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

class Request implements Runnable, Comparable<Request>, ProgressObserver, RequestInterface {
    private Integer id;
    private final EndpointImplementation rpc;
    private CallbackInterface<Object> callback;
    private final String name;
    private final int timeout;
    private RequestMethod ann;
    private float progress = 0;
    private int max = TimeStat.TICKS;
    private final Object[] args;
    private Type returnType;
    private Method method;
    private boolean batchFatal = true;
    private Object additionalControllerData = null;

    public Request(Integer id, EndpointImplementation rpc, Method method, String name, RequestMethod ann,
                   Object[] args, Type returnType, int timeout, CallbackInterface<Object> callback, Object additionalControllerData) {
        this.id = id;
        this.name = name;
        this.timeout = timeout;
        this.method = method;
        this.rpc = rpc;
        this.ann = ann;
        this.args = args;
        this.returnType = returnType;
        this.callback = callback;
        this.additionalControllerData = additionalControllerData;
    }

    @Override
    public void run() {
        try {
            Object result = rpc.getRequestConnector().call(this);

            invokeCallback(result);
        } catch (final Exception e) {
            invokeCallbackException(e);
            if (rpc.getErrorLogger() != null) {
                rpc.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        rpc.getErrorLogger().onError(e);
                    }
                });
            }
        }
    }

    public void invokeCallbackException(Exception e) {
        if (callback != null) {
            rpc.getHandler().post(new AsyncResult(callback, e));
        }
    }

    public void invokeCallback(Object result) {
        if (callback != null) {
            rpc.getHandler().post(new AsyncResult(callback, result));
        }
    }

    public static void invokeBatchCallback(final EndpointImplementation rpc, Batch<?> batch, final Exception e) {
        rpc.getHandler().post(new AsyncResult(batch, e));
    }

    public static void invokeBatchCallback(EndpointImplementation rpc, Batch<?> batch, Object[] results) {
        rpc.getHandler().post(new AsyncResult(batch, results));
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

    @Override
    public boolean isAllowEmptyResult() {
        return ann.allowEmptyResult();
    }

    @Override
    public Object getAdditionalData() {
        return additionalControllerData;
    }

    public Integer getTimeout() {
        return timeout;
    }

    LocalCache getLocalCache() {
        LocalCache ann = method.getAnnotation(LocalCache.class);
        if (ann == null) {
            ann = method.getDeclaringClass().getAnnotation(LocalCache.class);
        }
        if (ann != null && !ann.enabled()) {
            ann = null;
        }
        return ann;
    }

    ServerCache getServerCache() {
        ServerCache ann = method.getAnnotation(ServerCache.class);
        if (ann == null) {
            ann = method.getDeclaringClass().getAnnotation(ServerCache.class);
        }
        if (ann != null && !ann.enabled()) {
            ann = null;
        }
        return ann;
    }

    public int getLocalCacheLifeTime() {
        return getLocalCache().lifeTime();
    }

    public boolean isLocalCachable() {
        return getLocalCache() != null;
    }

    public int getLocalCacheSize() {
        return getLocalCache().size();
    }

    public LocalCacheLevel getLocalCacheLevel() {
        return getLocalCache().cacheLevel();
    }

    public boolean isLocalCacheOnlyOnError() {
        LocalCache localCache = getLocalCache();
        return localCache != null && localCache.onlyOnError();
    }

    public boolean isServerCachable() {
        return getServerCache() != null;
    }

    public int getServerCacheSize() {
        return getServerCache().size();
    }

    public ServerCacheLevel getServerCacheLevel() {
        return getServerCache().cacheLevel();
    }

    public boolean useServerCacheOldOnError() {
        return getServerCache().useOldOnError();
    }

    public long getWeight() {
        if (rpc.getStats().containsKey(name)) {
            return Math.max(rpc.getStats().get(name).avgTime, 1);
        } else {
            return timeout / 2;
        }
    }

    @Override
    public int compareTo(Request another) {
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

    @Override
    public void progressTick() {
        this.progress++;
        if (callback != null) {
            rpc.getHandler().post(new AsyncResult(callback, ((int) this.progress * 100 / max)));
        }
    }

    @Override
    public void progressTick(float progress) {
        this.progress += progress;
        if (callback != null) {
            rpc.getHandler().post(new AsyncResult(callback, ((int) this.progress * 100 / max)));
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

    public CallbackInterface<Object> getCallback() {
        return callback;
    }

    boolean isBatchFatal() {
        return batchFatal;
    }

    void setBatchFatal(boolean batchFatal) {
        this.batchFatal = batchFatal;
    }
}