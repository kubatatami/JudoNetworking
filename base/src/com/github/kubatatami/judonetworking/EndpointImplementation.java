package com.github.kubatatami.judonetworking;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

class EndpointImplementation implements Endpoint, EndpointClassic {

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
    private ErrorLogger errorLogger;
    private Clonner clonner = new ClonnerImplementation();
    private boolean test = false;
    private String testName = null;
    private int testRevision = 0;
    private int delay = 0;
    private String url;
    private ProtocolController protocolController;
    private HashMap<Class, VirtualServerInfo> virtualServers = new HashMap<Class, VirtualServerInfo>();
    private boolean verifyResultModel = false;
    private boolean processingMethod = false;
    private long tokenExpireTimestamp = -1;
    private Map<Integer,Request> singleCallMethods = new HashMap<Integer, Request>();
    private int id = 0;
    private ThreadPoolSizer threadPoolSizer=new DefaultThreadPoolSizer();
    private JudoExecutor executorService = new JudoExecutor();


    public EndpointImplementation(Context context, ProtocolController protocolController, TransportLayer transportLayer, String url) {
        init(context, protocolController, transportLayer, url);
    }


    private void init(Context context, ProtocolController protocolController, TransportLayer transportLayer, String url) {
        this.requestConnector = new RequestConnector(url, this, transportLayer);
        this.context = context;
        this.protocolController = protocolController;
        this.url = url;
        this.statFile = new File(context.getCacheDir(), "stats");
        this.memoryCache = new MemoryCacheImplementation(context);
        this.diskCache = new DiskCacheImplementation(context);
        NetworkUtils.addNetworkStateListener(context,new NetworkUtils.NetworkStateListener() {
            @Override
            public void onNetworkStateChange(NetworkInfo activeNetworkInfo) {
                setThreadPoolSize(threadPoolSizer.getThreadPoolSize(activeNetworkInfo));
            }
        });
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
    public void setTimeouts(int connectionTimeout, int methodTimeout, int reconnectionAttempts) {
        requestConnector.setConnectTimeout(connectionTimeout);
        requestConnector.setMethodTimeout(methodTimeout);
        requestConnector.setReconnections(reconnectionAttempts);
    }

    @Override
    public void setCallbackThread(boolean alwaysMainThread) {
        if (alwaysMainThread) {
            handler = new Handler(Looper.getMainLooper());
        } else {
            handler = new Handler();
        }
    }

    protected void setThreadPoolSize(int size) {
        executorService.setMaximumPoolSize(Math.max(2, size));
    }

    @Override
    public boolean isProcessingMethod() {
        return processingMethod;
    }

    @Override
    public void setProcessingMethod(boolean enabled) {
        this.processingMethod = enabled;
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
    public <T> AsyncResult sendAsyncRequest(String url, String name, CallbackInterface<T> callback, Object... args) {
        return sendAsyncRequest(url, name, new RequestOptions(), callback, args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> AsyncResult sendAsyncRequest(String url, String name, RequestOptions requestOptions, CallbackInterface<T> callback, Object... args) {
        Request request = new Request(
                ++id, this, null,
                name, requestOptions, args,
                ((ParameterizedType) callback.getClass().getGenericSuperclass()).getActualTypeArguments()[0], getRequestConnector().getMethodTimeout(),
                (CallbackInterface<Object>) callback, getProtocolController().getAdditionalRequestData());
        request.setCustomUrl(url);
        request.setApiKeyRequired(requestOptions.apiKeyRequired());
        filterNullArgs(request);
        Future<?> future = executorService.submit(request);
        request.setFuture(future);
        return request;
    }

    @SuppressWarnings("unchecked")
    public <T> T sendRequest(String url, String name, Type returnType, RequestOptions requestOptions, Object... args) throws JudoException {
        Request request = new Request(++id, this, null, name, requestOptions, args,
                returnType,
                getRequestConnector().getMethodTimeout(),
                null, getProtocolController().getAdditionalRequestData());
        request.setCustomUrl(url);
        request.setApiKeyRequired(requestOptions.apiKeyRequired());
        filterNullArgs(request);
        return (T) getRequestConnector().call(request);
    }



    public void filterNullArgs(Request request){
        if(request.getArgs()!=null){
            Annotation[][] paramAnnotations = ReflectionCache.getParameterAnnotations(request.getMethod());
            List<String> paramNames= new ArrayList<String>();
            List<Object> args = new ArrayList<Object>();
            Collections.addAll(paramNames, request.getParamNames());
            Collections.addAll(args, request.getArgs());
            for(int i=args.size()-1;i>=0;i--){
                if(args.get(i)==null){
                    boolean ignore=false;

                    IgnoreNullParam ignoreNullParam=ReflectionCache.findAnnotation(paramAnnotations[i], IgnoreNullParam.class);
                    if(ignoreNullParam!=null){
                        ignore=ignoreNullParam.value();
                    }else{
                        ignoreNullParam=ReflectionCache.getAnnotation(request.getMethod().getDeclaringClass(), IgnoreNullParam.class);
                        if(ignoreNullParam!=null){
                            ignore=ignoreNullParam.value();
                        }
                    }
                    if(ignore) {
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
                LoggerImpl.log("Batch starts from " +
                        stackTraceElement.getClassName() +
                        "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
            } catch (Exception ex) {
                LoggerImpl.log("Can't log stacktrace");
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
                LoggerImpl.log("Batch starts from " +
                        stackTraceElement.getClassName() +
                        "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
            } catch (Exception ex) {
                LoggerImpl.log("Can't log stacktrace");
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


    public TokenCaller getTokenCaller() {
        return protocolController.getTokenCaller();
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
    public void setErrorLogger(ErrorLogger logger) {
        this.errorLogger = logger;
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
                LoggerImpl.log(entry.getKey() + ":" + entry.getValue());
            }
        }
    }

    @Override
    public void clearTimeProfilerStat() {
        boolean result = statFile.delete();
        stats = Collections.synchronizedMap(new HashMap<String, MethodStat>());
    }


    @Override
    public void startTest(boolean onlyInDebugMode, String name, int revision) {

        String className = context.getApplicationContext().getPackageName() + ".BuildConfig";
        try {
            Class<?> clazz = Class.forName(className);

            Field field = clazz.getDeclaredField("DEBUG");

            Boolean debug = (Boolean) field.get(null);

            if (!onlyInDebugMode || debug) {
                this.test = true;
                this.testName = name;
                this.testRevision = revision;
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void setVerifyResultModel(boolean enabled) {
        verifyResultModel = enabled;
    }

    boolean isVerifyResultModel() {
        return verifyResultModel;
    }

    String getTestName() {
        return testName;
    }

    int getTestRevision() {
        return testRevision;
    }

    @Override
    public void stopTest() {
        this.test = false;
    }

    public BatchTimeoutMode getTimeoutMode() {
        return timeoutMode;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }


    void setMemoryCache(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
    }


    public DiskCache getDiskCache() {
        return diskCache;
    }


    public MemoryCache getMemoryCache() {
        return memoryCache;
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
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD && Runtime.getRuntime().availableProcessors()>1) {
            System.gc(); //concurrent gc for lower latency (experiment)
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new CancelException();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, MethodStat> getStats() {
        if (stats == null) {
            if (statFile.exists() && statFile.length() < maxStatFileSize * 1024) {

                try {
                    FileInputStream fileStream = new FileInputStream(statFile);
                    ObjectInputStream os = new ObjectInputStream(fileStream);
                    stats = (Map<String, MethodStat>) os.readObject();
                    os.close();
                } catch (Exception e) {
                    LoggerImpl.log(e);
                    stats = Collections.synchronizedMap(new HashMap<String, MethodStat>());
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
            LoggerImpl.log(e);
        }

    }

    public long getTokenExpireTimestamp() {
        return tokenExpireTimestamp;
    }

    public void setTokenExpireTimestamp(long tokenExpireTimestamp) {
        this.tokenExpireTimestamp = tokenExpireTimestamp;
    }

    CacheMode getCacheMode() {
        return cacheMode;
    }

    public Context getContext() {
        return context;
    }

    public ErrorLogger getErrorLogger() {
        return errorLogger;
    }

    public Clonner getClonner() {
        return clonner;
    }

    public void setClonner(Clonner clonner) {
        this.clonner = clonner;
    }

    public String getUrl() {
        return url;
    }

    boolean isTest() {
        return test;
    }

    public ProtocolController getProtocolController() {
        return protocolController;
    }

    public float getPercentLoss() {
        return percentLoss;
    }

    public Map<Integer,Request> getSingleCallMethods() {
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
}
