package com.github.kubatatami.judonetworking.internals;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.EndpointClassic;
import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.annotations.IgnoreNullParam;
import com.github.kubatatami.judonetworking.annotations.LocalCache;
import com.github.kubatatami.judonetworking.batches.Batch;
import com.github.kubatatami.judonetworking.caches.DefaultDiskCache;
import com.github.kubatatami.judonetworking.caches.DefaultMemoryCache;
import com.github.kubatatami.judonetworking.caches.DiskCache;
import com.github.kubatatami.judonetworking.caches.MemoryCache;
import com.github.kubatatami.judonetworking.callbacks.Callback;
import com.github.kubatatami.judonetworking.clonners.Clonner;
import com.github.kubatatami.judonetworking.clonners.DefaultClonner;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.exceptions.CancelException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.executors.JudoExecutor;
import com.github.kubatatami.judonetworking.internals.requests.RequestImpl;
import com.github.kubatatami.judonetworking.internals.requests.RequestOptions;
import com.github.kubatatami.judonetworking.internals.stats.MethodStat;
import com.github.kubatatami.judonetworking.internals.virtuals.VirtualServerInfo;
import com.github.kubatatami.judonetworking.logs.ErrorLogger;
import com.github.kubatatami.judonetworking.logs.JudoLogger;
import com.github.kubatatami.judonetworking.threads.DefaultThreadPoolSizer;
import com.github.kubatatami.judonetworking.threads.ThreadPoolSizer;
import com.github.kubatatami.judonetworking.transports.TransportLayer;
import com.github.kubatatami.judonetworking.utils.NetworkUtils;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public class EndpointImpl implements Endpoint, EndpointClassic {

    private RequestConnector requestConnector;

    private Handler handler = new Handler();

    private Context context;

    private boolean cacheEnabled = false;

    private CacheMode cacheMode = CacheMode.NORMAL;

    private boolean timeProfiler = false;

    private BatchTimeoutMode timeoutMode = BatchTimeoutMode.TIMEOUTS_SUM;

    private MemoryCache memoryCache;

    private DiskCache diskCache;

    private int debugFlags = 0;

    private Map<String, MethodStat> stats;

    private File statFile;

    private float percentLoss;

    private int maxStatFileSize = 50; //KB

    private Set<ErrorLogger> errorLoggers = new HashSet<>();

    private Clonner clonner = new DefaultClonner();

    private int delay = 0;

    private String url;

    private ProtocolController protocolController;

    private HashMap<Class, VirtualServerInfo> virtualServers = new HashMap<>();

    private Map<Integer, RequestImpl> singleCallMethods = new HashMap<>();

    private Set<Integer> requestIds = Collections.synchronizedSet(new HashSet<Integer>());

    private int id = 0;

    private ThreadPoolSizer threadPoolSizer = new DefaultThreadPoolSizer();

    private JudoExecutor executorService = new JudoExecutor(this);

    private UrlModifier urlModifier;

    private OnRequestEventListener onRequestEventListener;

    private int defaultMethodCacheLifeTime = LocalCache.INFINITE;

    private int defaultMethodCacheSize = LocalCache.INFINITE;

    private LocalCache.CacheLevel defaultMethodCacheLevel = LocalCache.CacheLevel.MEMORY_ONLY;

    private LocalCache.OnlyOnError defaultMethodCacheOnlyOnErrorMode = LocalCache.OnlyOnError.NO;

    public EndpointImpl(Context context, ProtocolController protocolController, TransportLayer transportLayer, String url) {
        init(context, protocolController, transportLayer, url);
    }
    
    private void init(Context context, ProtocolController protocolController, TransportLayer transportLayer, String url) {
        this.requestConnector = new RequestConnector(this, transportLayer);
        this.context = context;
        this.protocolController = protocolController;
        this.url = url;
        this.statFile = new File(context.getCacheDir(), "stats");
        this.memoryCache = new DefaultMemoryCache(context);
        this.diskCache = new DefaultDiskCache(context);
    }

    public HashMap<Class, VirtualServerInfo> getVirtualServers() {
        return virtualServers;
    }

    public JudoExecutor getExecutorService() {
        return executorService;
    }

    @Override
    public <T> void registerVirtualServer(Class<T> type, T virtualServer, int delay) {
        virtualServers.put(type, new VirtualServerInfo(virtualServer, delay, delay));
    }

    @Override
    public <T> void registerVirtualServer(Class<T> type, T virtualServer) {
        virtualServers.put(type, new VirtualServerInfo(virtualServer, 0, 0));
    }

    @Override
    public <T> void registerVirtualServer(Class<T> type, T virtualServer, int minDelay, int maxDelay) {
        virtualServers.put(type, new VirtualServerInfo(virtualServer, minDelay, maxDelay));
    }

    @Override
    public <T> void unregisterVirtualServer(Class<T> type) {
        virtualServers.remove(type);
    }

    @Override
    public boolean isIdleNow() {
        return requestIds.size() == 0;
    }

    @Override
    public void setTimeouts(int connectionTimeout, int methodTimeout) {
        requestConnector.setConnectTimeout(connectionTimeout);
        requestConnector.setMethodTimeout(methodTimeout);
    }

    @Override
    public void setCallbackThread(boolean alwaysMainThread) {
        if (alwaysMainThread) {
            handler = new Handler(Looper.getMainLooper());
        } else {
            handler = new Handler();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> obj) {
        return getService(obj, new RequestProxy(this, protocolController.getAutoBatchTime() > 0 ? BatchMode.AUTO : BatchMode.NONE, null));
    }

    @SuppressWarnings("unchecked")
    private <T> T getService(Class<T> obj, RequestProxy proxy) {
        return (T) Proxy.newProxyInstance(obj.getClassLoader(), new Class<?>[]{obj}, proxy);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> AsyncResult sendAsyncRequest(String url, String name, Callback<T> callback, Object... args) {
        return sendAsyncRequest(url, name, new RequestOptions(), callback, args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> AsyncResult sendAsyncRequest(String url, String name, RequestOptions requestOptions, Callback<T> callback, Object... args) {
        Type returnType = ((ParameterizedType) callback.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        RequestImpl request = new RequestImpl(
                ++id, this, null,
                name, requestOptions, args,
                returnType, getRequestConnector().getMethodTimeout(),
                (Callback<Object>) callback, getProtocolController().getAdditionalRequestData());
        request.setCustomUrl(url);
        request.setApiKeyRequired(requestOptions.apiKeyRequired());
        filterNullArgs(request);
        Future<?> future = executorService.submit(request);
        request.setFuture(future);
        return request;
    }

    @SuppressWarnings("unchecked")
    public <T> T sendRequest(String url, String name, Type returnType, RequestOptions requestOptions, Object... args) throws JudoException {
        RequestImpl request = new RequestImpl(++id, this, null, name, requestOptions, args,
                returnType,
                getRequestConnector().getMethodTimeout(),
                null, getProtocolController().getAdditionalRequestData());
        request.setCustomUrl(url);
        request.setApiKeyRequired(requestOptions.apiKeyRequired());
        filterNullArgs(request);
        return (T) getRequestConnector().call(request);
    }


    public void filterNullArgs(RequestImpl request) {
        if (request.getArgs() != null) {
            Annotation[][] paramAnnotations = ReflectionCache.getParameterAnnotations(request.getMethod());
            List<String> paramNames = new ArrayList<>();
            List<Object> args = new ArrayList<>();
            Collections.addAll(paramNames, request.getParamNames());
            Collections.addAll(args, request.getArgs());
            for (int i = args.size() - 1; i >= 0; i--) {
                if (args.get(i) == null) {
                    boolean ignore = false;

                    IgnoreNullParam ignoreNullParam = ReflectionCache.findAnnotation(paramAnnotations[i], IgnoreNullParam.class);
                    if (ignoreNullParam != null) {
                        ignore = ignoreNullParam.value();
                    } else {
                        ignoreNullParam = ReflectionCache.getAnnotation(request.getMethod().getDeclaringClass(), IgnoreNullParam.class);
                        if (ignoreNullParam != null) {
                            ignore = ignoreNullParam.value();
                        }
                    }
                    if (ignore) {
                        args.remove(i);
                        if (paramNames.size() > i) {
                            paramNames.remove(i);
                        }
                    }
                }
            }
            request.setArgs(args.toArray());
            request.setParamNames(paramNames.toArray(new String[paramNames.size()]));
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> AsyncResult callInBatch(final Class<T> obj, final Batch<T> batch) {

        if ((getDebugFlags() & REQUEST_LINE_DEBUG) > 0) {
            try {
                StackTraceElement stackTraceElement = RequestProxy.getExternalStacktrace(Thread.currentThread().getStackTrace());
                JudoLogger.log("Batch starts from " +
                        stackTraceElement.getClassName() +
                        "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")", JudoLogger.LogLevel.DEBUG);
            } catch (Exception ex) {
                JudoLogger.log("Can't log stacktrace", JudoLogger.LogLevel.ASSERT);
            }
        }

        final RequestProxy pr = new RequestProxy(this, BatchMode.MANUAL, batch);
        T proxy = getService(obj, pr);
        pr.setBatchFatal(true);
        batch.run(proxy);
        pr.setBatchFatal(false);
        batch.runNonFatal(proxy);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                pr.callBatch();
            }
        });
        return pr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> AsyncResult callAsyncInBatch(final Class<T> obj, final Batch<T> batch) {

        if ((getDebugFlags() & REQUEST_LINE_DEBUG) > 0) {
            try {
                StackTraceElement stackTraceElement = RequestProxy.getExternalStacktrace(Thread.currentThread().getStackTrace());
                JudoLogger.log("Batch starts from " +
                        stackTraceElement.getClassName() +
                        "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")", JudoLogger.LogLevel.DEBUG);
            } catch (Exception ex) {
                JudoLogger.log("Can't log stacktrace", JudoLogger.LogLevel.ASSERT);
            }
        }

        final RequestProxy pr = new RequestProxy(this, BatchMode.MANUAL, batch);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                T proxy = getService(obj, pr);
                pr.setBatchFatal(true);
                batch.run(proxy);
                pr.setBatchFatal(false);
                batch.runNonFatal(proxy);
                pr.callBatch();
            }
        });
        return pr;
    }

    public Handler getHandler() {
        return handler;
    }

    public RequestConnector getRequestConnector() {
        return requestConnector;
    }


    @Override
    public void setBatchTimeoutMode(BatchTimeoutMode mode) {
        this.timeoutMode = mode;
    }

    @Override
    public void setCacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
    }

    @Override
    public void addErrorLogger(ErrorLogger logger) {
        errorLoggers.add(logger);
    }

    @Override
    public void removeErrorLogger(ErrorLogger logger) {
        errorLoggers.remove(logger);
    }

    public void startRequest(Request request) {
        requestIds.add(request.getId());
        if (onRequestEventListener != null) {
            onRequestEventListener.onStart(request, requestIds.size());
        }
    }

    public void stopRequest(Request request) {
        requestIds.remove(request.getId());
        if (onRequestEventListener != null) {
            onRequestEventListener.onStop(request, requestIds.size());
        }
    }

    @Override
    public void setOnRequestEventListener(OnRequestEventListener onRequestEventListener) {
        this.onRequestEventListener = onRequestEventListener;
    }

    @Override
    public void setCacheMode(CacheMode mode) {
        this.cacheMode = mode;
    }

    @Override
    public void setTimeProfilerEnabled(boolean enabled) {
        this.timeProfiler = enabled;
    }

    public int getMaxStatFileSize() {
        return maxStatFileSize;
    }

    public void setMaxStatFileSize(int maxStatFileSize) {
        this.maxStatFileSize = maxStatFileSize;
    }

    @Override
    public void showTimeProfilerInfo() {
        if (stats != null) {
            for (Map.Entry<String, MethodStat> entry : stats.entrySet()) {
                JudoLogger.log(entry.getKey() + ":" + entry.getValue(), JudoLogger.LogLevel.INFO);
            }
        }
    }

    @Override
    public void clearTimeProfilerStat() {
        boolean result = statFile.delete();
        if (result) {
            JudoLogger.log("Can't remove stats file", JudoLogger.LogLevel.ASSERT);
        }
        stats = Collections.synchronizedMap(new HashMap<String, MethodStat>());
    }

    public BatchTimeoutMode getTimeoutMode() {
        return timeoutMode;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }


    public void setMemoryCache(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
    }

    @Override
    public void setDiskCache(DiskCache diskCache) {
        this.diskCache = diskCache;
    }


    public DiskCache getDiskCache() {
        return diskCache;
    }


    public MemoryCache getMemoryCache() {
        return memoryCache;
    }

    @Override
    public void clearCache() {
        getMemoryCache().clearCache();
        getDiskCache().clearCache();
    }

    @Override
    public void setPercentLoss(float percentLoss) {
        this.percentLoss = percentLoss;
    }

    @Override
    public void setDebugFlags(int flags) {
        this.debugFlags = flags;
        if (memoryCache != null) {
            memoryCache.setDebugFlags(flags);
        }

        if (diskCache != null) {
            diskCache.setDebugFlags(flags);
        }
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public int getDebugFlags() {
        return debugFlags;
    }

    public boolean isTimeProfiler() {
        return timeProfiler;
    }

    public static void checkThread() throws CancelException {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancelException();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, MethodStat> getStats() {
        if (stats == null) {
            if (statFile.exists() && statFile.length() < maxStatFileSize * 1024) {
                FileInputStream fileStream = null;
                ObjectInputStream os = null;
                try {
                    fileStream = new FileInputStream(statFile);
                    os = new ObjectInputStream(fileStream);
                    stats = (Map<String, MethodStat>) os.readObject();

                } catch (Exception e) {
                    JudoLogger.log(e);
                    stats = Collections.synchronizedMap(new HashMap<String, MethodStat>());
                } finally {
                    try {
                        if (os != null) {
                            os.close();
                        }
                        if (fileStream != null) {
                            fileStream.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                stats = Collections.synchronizedMap(new HashMap<String, MethodStat>());
            }
        }
        return stats;
    }

    public void saveStat() {
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(statFile));
            os.writeObject(stats);
            os.flush();
            os.close();
        } catch (IOException e) {
            JudoLogger.log(e);
        }

    }

    CacheMode getCacheMode() {
        return cacheMode;
    }

    public Context getContext() {
        return context;
    }

    public Set<ErrorLogger> getErrorLoggers() {
        return errorLoggers;
    }

    public Clonner getClonner() {
        return clonner;
    }

    public void setClonner(Clonner clonner) {
        this.clonner = clonner;
    }

    public String getUrl() {
        if (urlModifier != null) {
            return urlModifier.createUrl(url);
        } else {
            return url;
        }
    }

    public ProtocolController getProtocolController() {
        return protocolController;
    }

    public float getPercentLoss() {
        return percentLoss;
    }

    public Map<Integer, RequestImpl> getSingleCallMethods() {
        return singleCallMethods;
    }

    public int getThreadPriority() {
        return executorService.getThreadPriority();
    }

    public void setThreadPriority(int threadPriority) {
        executorService.setThreadPriority(threadPriority);
    }

    public void setThreadPoolSizer(ThreadPoolSizer threadPoolSizer) {
        this.threadPoolSizer = threadPoolSizer;
    }

    public int getBestConnectionsSize() {
        return this.threadPoolSizer.getThreadPoolSize(context, NetworkUtils.getActiveNetworkInfo(context));
    }

    public void setUrlModifier(UrlModifier urlModifier) {
        this.urlModifier = urlModifier;
    }

    enum BatchMode {
        NONE, MANUAL, AUTO
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int getDefaultMethodCacheLifeTime() {
        return defaultMethodCacheLifeTime;
    }

    @Override
    public void setDefaultMethodCacheLifeTime(int millis) {
        this.defaultMethodCacheLifeTime = millis;
    }

    @Override
    public int getDefaultMethodCacheSize() {
        return defaultMethodCacheSize;
    }

    @Override
    public void setDefaultMethodCacheSize(int millis) {
        this.defaultMethodCacheSize = millis;
    }

    @Override
    public LocalCache.CacheLevel getDefaultMethodCacheLevel() {
        return defaultMethodCacheLevel;
    }

    @Override
    public void setDefaultMethodCacheLevel(LocalCache.CacheLevel defaultMethodCacheLevel) {
        this.defaultMethodCacheLevel = defaultMethodCacheLevel;
    }

    @Override
    public LocalCache.OnlyOnError getDefaultMethodCacheOnlyOnErrorMode() {
        return defaultMethodCacheOnlyOnErrorMode;
    }

    @Override
    public void setDefaultMethodCacheOnlyOnErrorMode(LocalCache.OnlyOnError defaultMethodCacheOnlyOnErrorMode) {
        this.defaultMethodCacheOnlyOnErrorMode = defaultMethodCacheOnlyOnErrorMode;
    }
}
