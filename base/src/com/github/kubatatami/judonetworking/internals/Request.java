package com.github.kubatatami.judonetworking.internals;

import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.annotations.ApiKeyRequired;
import com.github.kubatatami.judonetworking.annotations.Delay;
import com.github.kubatatami.judonetworking.annotations.LocalCache;
import com.github.kubatatami.judonetworking.annotations.RequestMethod;
import com.github.kubatatami.judonetworking.annotations.ServerCache;
import com.github.kubatatami.judonetworking.annotations.SingleCall;
import com.github.kubatatami.judonetworking.callbacks.CallbackInterface;
import com.github.kubatatami.judonetworking.exceptions.CancelException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.cache.CacheInfo;
import com.github.kubatatami.judonetworking.internals.cache.CacheMethod;
import com.github.kubatatami.judonetworking.internals.stats.TimeStat;
import com.github.kubatatami.judonetworking.internals.utils.ReflectionCache;
import com.github.kubatatami.judonetworking.logs.ErrorLogger;
import com.github.kubatatami.judonetworking.logs.JudoLogger;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.Future;

public class Request implements Runnable, Comparable<Request>, ProgressObserver, RequestInterface, AsyncResult {
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
    private Class<?> apiInterface;
    private boolean batchFatal = true;
    private Serializable additionalControllerData = null;
    private boolean cancelled, done, running;
    private boolean isApiKeyRequired;
    private String customUrl;
    private Future<?> future;


    public Request(Integer id, EndpointImplementation rpc, Method method, String name, RequestMethod ann,
                   Object[] args, Type returnType, int timeout, CallbackInterface<Object> callback,
                   Serializable additionalControllerData) {
        this.id = id;
        this.name = name;
        this.timeout = timeout;
        this.method = method;
        if(method!=null) {
            this.apiInterface = method.getDeclaringClass();
        }
        this.rpc = rpc;
        this.ann = ann;
        this.args = args;
        this.returnType = returnType;
        this.callback = callback;
        this.additionalControllerData = additionalControllerData;
        this.paramNames = ann.paramNames();
    }

    @Override
    public void run() {
        try {
            if(!cancelled) {
                Object result = rpc.getRequestConnector().call(this);
                invokeCallback(result);
            }
        } catch (final JudoException e) {
            invokeCallbackException(e);
            if (rpc.getErrorLoggers().size() != 0 && !(e instanceof CancelException)) {
                rpc.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        for(ErrorLogger errorLogger : rpc.getErrorLoggers()) {
                            errorLogger.onError(e, Request.this);
                        }
                    }
                });
            }
        }
    }

    public void invokeStart(CacheInfo cacheInfo) {
        if (callback != null) {
            rpc.getHandler().post(new AsyncResultSender(this, cacheInfo));
        }
    }

    public void invokeCallbackException(JudoException e) {
        rpc.getHandler().post(new AsyncResultSender(this, e));
    }

    public void invokeCallback(Object result) {
        rpc.getHandler().post(new AsyncResultSender(this, result));
    }

    public static void invokeBatchCallbackStart(final EndpointImplementation rpc, RequestProxy requestProxy) {
        rpc.getHandler().post(new AsyncResultSender(rpc, requestProxy));
    }

    public static void invokeBatchCallbackProgress(final EndpointImplementation rpc, RequestProxy requestProxy, int progress) {
        rpc.getHandler().post(new AsyncResultSender(rpc, requestProxy, progress));
    }

    public static void invokeBatchCallbackException(final EndpointImplementation rpc, RequestProxy requestProxy, final JudoException e) {
        rpc.getHandler().post(new AsyncResultSender(rpc, requestProxy, e));
    }

    public static void invokeBatchCallback(EndpointImplementation rpc, RequestProxy requestProxy, Object[] results) {
        rpc.getHandler().post(new AsyncResultSender(rpc, requestProxy, results));
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
    public int getMethodId() {
        return CacheMethod.getMethodId(method);
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
        if (method != null) {
            ApiKeyRequired ann = ReflectionCache.getAnnotation(method, ApiKeyRequired.class);
            if (ann == null) {
                ann = ReflectionCache.getAnnotation(apiInterface,ApiKeyRequired.class);
            }
            return ann != null && ann.enabled();
        } else {
            return isApiKeyRequired;
        }
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
    public Serializable getAdditionalData() {
        return additionalControllerData;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public LocalCache getLocalCache() {
        if (method != null) {
            LocalCache ann = ReflectionCache.getAnnotationInherited(method,LocalCache.class);
            if (ann != null && !ann.enabled()) {
                ann = null;
            }
            return ann;
        } else {
            return null;
        }
    }

    public int getDelay() {
        if (method != null) {
            Delay ann = ReflectionCache.getAnnotationInherited(method,Delay.class);
            if (ann != null && !ann.enabled()) {
                ann = null;
            }
            return ann != null ? ann.value() : 0;
        } else {
            return 0;
        }
    }

    public ServerCache getServerCache() {
        if (method != null) {
            ServerCache ann = ReflectionCache.getAnnotationInherited(method,ServerCache.class);
            if (ann != null && !ann.enabled()) {
                ann = null;
            }
            return ann;
        } else {
            return null;
        }
    }

    public SingleCall getSingleCall() {
        if (method != null) {
            SingleCall ann = ReflectionCache.getAnnotationInherited(method,SingleCall.class);
            if (ann != null && !ann.enabled()) {
                ann = null;
            }
            return ann;
        } else {
            return null;
        }
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

    public LocalCache.CacheLevel getLocalCacheLevel() {
        return getLocalCache().cacheLevel();
    }


    public LocalCache.OnlyOnError getLocalCacheOnlyOnErrorMode() {
        LocalCache localCache = getLocalCache();
        return localCache != null ? localCache.onlyOnError() : LocalCache.OnlyOnError.NO;
    }

    public boolean isServerCachable() {
        return getServerCache() != null;
    }

    public int getServerCacheSize() {
        return getServerCache().size();
    }

    public ServerCache.CacheLevel getServerCacheLevel() {
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
            rpc.getHandler().post(new AsyncResultSender(this, ((int) this.progress * 100 / max)));
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

    public void setAdditionalControllerData(Serializable additionalControllerData) {
        this.additionalControllerData = additionalControllerData;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        if(done){
            return;
        }
        this.cancelled = true;
        if ((rpc.getDebugFlags() & Endpoint.CANCEL_DEBUG) > 0) {
            JudoLogger.log("Request " + name + " cancelled.");
        }
        if (running) {
            running = false;
            synchronized (rpc.getSingleCallMethods()) {
                rpc.getSingleCallMethods().remove(CacheMethod.getMethodId(method));
            }
            if (future != null) {
                future.cancel(true);
            }
            rpc.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.onFinish();
                }
            });
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public void done() {
        this.done = true;
        this.running = false;
    }

    public void start() {
        this.running = true;
    }

    public String getCustomUrl() {
        return customUrl;
    }

    public void setCustomUrl(String customUrl) {
        this.customUrl = customUrl;
    }

    public void setApiKeyRequired(boolean isApiKeyRequired) {
        this.isApiKeyRequired = isApiKeyRequired;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }
}