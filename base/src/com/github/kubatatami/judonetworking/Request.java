package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

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
    private Object[] args;
    private String[] paramNames;
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
        this.paramNames = ann.paramNames();
        this.args = args;
        this.returnType = returnType;
        this.callback = callback;
        this.additionalControllerData = additionalControllerData;
    }

    @Override
    public void run() {
        try {
            invokeStart();
            Object result = rpc.getRequestConnector().call(this);
            invokeCallback(result);
        } catch (final JudoException e) {
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

    public void invokeStart() {
        if (callback != null) {
            rpc.getHandler().post(new AsyncResult(this));
        }
    }

    public void invokeCallbackException(JudoException e) {
        rpc.getHandler().post(new AsyncResult(this, e));
    }

    public void invokeCallback(Object result) {
        rpc.getHandler().post(new AsyncResult(this, result));
    }

    public void invokeProgress(int progress) {
        if (callback != null) {
            rpc.getHandler().post(new AsyncResult(this, progress));
        }
    }

    public static void invokeBatchCallbackProgress(final EndpointImplementation rpc, Batch<?> batch, int progress) {
        rpc.getHandler().post(new AsyncResult(rpc, batch, progress));
    }

    public static void invokeBatchCallbackException(final EndpointImplementation rpc, Batch<?> batch, final JudoException e) {
        rpc.getHandler().post(new AsyncResult(rpc, batch, e));
    }

    public static void invokeBatchCallback(EndpointImplementation rpc, Batch<?> batch, Object[] results) {
        rpc.getHandler().post(new AsyncResult(rpc, batch, results));
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
        return paramNames;
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
    public boolean isApiKeyRequired() {
        ApiKeyRequired ann = method.getAnnotation(ApiKeyRequired.class);
        if (ann == null) {
            ann = method.getDeclaringClass().getAnnotation(ApiKeyRequired.class);
        }
        return ann != null && ann.enabled();
    }

    @Override
    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
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

    boolean isSingleCall() {
        SingleCall ann = method.getAnnotation(SingleCall.class);
        if (ann == null) {
            ann = method.getDeclaringClass().getAnnotation(SingleCall.class);
        }
        return ann != null && ann.enabled();
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
    public void clearProgress() {
        this.progress = 0;
        tick();
    }

    @Override
    public void progressTick() {
        this.progress++;
        tick();
    }

    @Override
    public void progressTick(float progress) {
        this.progress += progress;
        tick();
    }

    private void tick() {
        if (callback != null) {
            rpc.getHandler().post(new AsyncResult(this, ((int) this.progress * 100 / max)));
        }
    }

    public EndpointImplementation getRpc() {
        return rpc;
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

    public void setAdditionalControllerData(Object additionalControllerData) {
        this.additionalControllerData = additionalControllerData;
    }
}