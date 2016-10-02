package com.github.kubatatami.judonetworking.internals;


import android.util.Base64;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.annotations.Base64Param;
import com.github.kubatatami.judonetworking.annotations.LocalCache;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.exceptions.CancelException;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.cache.CacheMethod;
import com.github.kubatatami.judonetworking.internals.requests.RequestImpl;
import com.github.kubatatami.judonetworking.internals.results.CacheResult;
import com.github.kubatatami.judonetworking.internals.results.ErrorResult;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.internals.results.RequestSuccessResult;
import com.github.kubatatami.judonetworking.internals.stats.MethodStat;
import com.github.kubatatami.judonetworking.internals.stats.TimeStat;
import com.github.kubatatami.judonetworking.internals.streams.RequestInputStream;
import com.github.kubatatami.judonetworking.internals.virtuals.VirtualCallback;
import com.github.kubatatami.judonetworking.internals.virtuals.VirtualServerInfo;
import com.github.kubatatami.judonetworking.logs.JudoLogger;
import com.github.kubatatami.judonetworking.transports.TransportLayer;
import com.github.kubatatami.judonetworking.utils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RequestConnector {

    private final EndpointImpl rpc;

    private final TransportLayer transportLayer;

    private final Random randomGenerator = new Random();

    public RequestConnector(EndpointImpl rpc, TransportLayer transportLayer) {
        this.rpc = rpc;
        this.transportLayer = transportLayer;
    }

    private static void longLog(String tag, String message, JudoLogger.LogLevel level) {
        JudoLogger.longLog(tag, message, level);
    }

    private RequestResult sendRequest(RequestImpl request, TimeStat timeStat) {
        return sendRequest(request, timeStat, false);
    }

    private RequestResult sendRequest(RequestImpl request, TimeStat timeStat, boolean ignoreTokenError) {
        try {
            RequestResult result;
            ProtocolController controller = rpc.getProtocolController();
            result = handleVirtualServerRequest(request, timeStat);
            if (result != null) {
                if (result.error != null) {
                    return result;
                }
            } else {
                ProtocolController.RequestInfo requestInfo = controller.createRequest(
                        request.getCustomUrl() == null ? rpc.getUrl() : request.getCustomUrl(),
                        request);
                timeStat.tickCreateTime();
                lossCheck();
                EndpointImpl.checkThread();
                delay(request.getDelay());
                EndpointImpl.checkThread();
                TransportLayer.Connection conn = transportLayer.send(request.getName(), controller, requestInfo, request.getTimeout(), timeStat,
                        rpc.getDebugFlags(), request.getMethod());
                EndpointImpl.checkThread();

                InputStream connectionStream = conn.getStream();
                if ((rpc.getDebugFlags() & Endpoint.RESPONSE_DEBUG) > 0) {
                    String resStr = FileUtils.convertStreamToString(conn.getStream());
                    longLog("Response body(" + request.getName() + ", " + resStr.length() + " Bytes)", resStr, JudoLogger.LogLevel.INFO);
                    connectionStream = new ByteArrayInputStream(resStr.getBytes());
                }
                RequestInputStream stream = new RequestInputStream(connectionStream, timeStat, conn.getContentLength());
                EndpointImpl.checkThread();
                request.setHeaders(conn.getHeaders());
                result = controller.parseResponse(request, stream, conn.getHeaders());
                EndpointImpl.checkThread();
                try {
                    stream.close();
                } catch (Exception ignored) {
                }
                timeStat.tickParseTime();
                conn.close();
            }
            return result;
        } catch (JudoException e) {
            return new ErrorResult(request.getId(), e);
        } catch (Exception e) {
            return new ErrorResult(request.getId(), new JudoException(e));
        }
    }

    public static Object[] addElement(Object[] org, Object added) {
        Object[] result = new Object[org.length + 1];
        System.arraycopy(org, 0, result, 0, org.length);
        result[org.length] = added;
        return result;
    }


    private RequestResult handleVirtualServerRequest(RequestImpl request, TimeStat timeStat) throws JudoException {
        try {
            if (request.getMethod() != null) {
                VirtualServerInfo virtualServerInfo = rpc.getVirtualServers().get(request.getMethod().getDeclaringClass());
                if (virtualServerInfo != null) {
                    if (request.getCallback() == null) {
                        try {
                            Object object = request.getMethod().invoke(virtualServerInfo.server, request.getArgs());
                            int delay = randDelay(virtualServerInfo.minDelay, virtualServerInfo.maxDelay);
                            for (int i = 0; i <= TimeStat.TICKS; i++) {
                                Thread.sleep(delay / TimeStat.TICKS);
                                timeStat.tickTime(i);
                            }
                            return new RequestSuccessResult(request.getId(), object);
                        } catch (InvocationTargetException ex) {
                            if (ex.getCause() == null || !(ex.getCause() instanceof UnsupportedOperationException)) {
                                throw ex;
                            }
                        }

                    } else {
                        VirtualCallback callback = new VirtualCallback(request.getId());
                        Object[] args = request.getArgs() != null ? addElement(request.getArgs(), callback) : new Object[]{callback};
                        boolean implemented = true;
                        try {
                            AsyncResult asyncResult = (AsyncResult) request.getMethod().invoke(virtualServerInfo.server, args);
                            if (asyncResult != null) {
                                request.setHeaders(asyncResult.getHeaders());
                            }
                            int delay = randDelay(virtualServerInfo.minDelay, virtualServerInfo.maxDelay);
                            for (int i = 0; i <= TimeStat.TICKS; i++) {
                                Thread.sleep(delay / TimeStat.TICKS);
                                timeStat.tickTime(i);
                            }
                        } catch (InvocationTargetException ex) {
                            if (ex.getCause() != null && ex.getCause() instanceof UnsupportedOperationException) {
                                implemented = false;
                            } else {
                                throw ex;
                            }
                        }
                        if (implemented) {
                            return callback.getResult();
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new JudoException("Can't invoke virtual server", e);
        }
    }

    protected Base64Param findBase64Annotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Base64Param) {
                return (Base64Param) annotation;
            }
        }
        return null;
    }

    protected void findAndCreateBase64(RequestImpl request) {
        if (request.getArgs() != null) {
            int i = 0;
            if (request.getMethod() != null) {
                Annotation[][] annotations = request.getMethod().getParameterAnnotations();
                for (Object object : request.getArgs()) {

                    if (object instanceof byte[]) {
                        Base64Param ann = findBase64Annotation(annotations[i]);
                        if (ann != null) {
                            request.getArgs()[i] = ann.prefix() + Base64.encodeToString((byte[]) object, ann.type()) + ann.suffix();
                        }
                    }
                    i++;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Object call(RequestImpl request) throws JudoException {
        try {

            CacheResult localCacheObject = null;
            TimeStat timeStat = new TimeStat(request);


            if ((rpc.isCacheEnabled() && request.isLocalCacheable())) {
                LocalCache.CacheLevel cacheLevel = request.getLocalCacheLevel();
                localCacheObject = rpc.getMemoryCache().get(request.getMethodId(), request.getArgs(), request.getLocalCacheLifeTime(), request.getLocalCacheSize());
                if (localCacheObject.result) {
                    if (request.getLocalCacheOnlyOnErrorMode().equals(LocalCache.OnlyOnError.NO)) {
                        request.invokeStart(new CacheInfo(true, localCacheObject.time));
                        request.setHeaders(localCacheObject.headers);
                        timeStat.tickCacheTime();
                        if (rpc.getCacheMode() == Endpoint.CacheMode.CLONE) {
                            localCacheObject.object = rpc.getClonner().clone(localCacheObject.object);
                        }
                        return localCacheObject.object;
                    }
                } else if (cacheLevel != LocalCache.CacheLevel.MEMORY_ONLY) {
                    CacheMethod cacheMethod = new CacheMethod(CacheMethod.getMethodId(request.getMethod()), request.getName(), request.getMethod().getDeclaringClass().getSimpleName(), rpc.getUrl(), cacheLevel);
                    localCacheObject = rpc.getDiskCache().get(cacheMethod, Arrays.deepToString(request.getArgs()), request.getLocalCacheLifeTime());
                    if (localCacheObject.result) {
                        rpc.getMemoryCache().put(request.getMethodId(),
                                request.getArgs(),
                                localCacheObject.object,
                                request.getLocalCacheSize(),
                                localCacheObject.headers);
                        if (request.getLocalCacheOnlyOnErrorMode().equals(LocalCache.OnlyOnError.NO)) {
                            request.invokeStart(new CacheInfo(true, localCacheObject.time));
                            request.setHeaders(localCacheObject.headers);
                            timeStat.tickCacheTime();
                            return localCacheObject.object;
                        }
                    }

                }
            }


            findAndCreateBase64(request);
            request.invokeStart(new CacheInfo(false, 0L));
            RequestResult result = sendRequest(request, timeStat);

            if (result instanceof ErrorResult) {
                if (localCacheObject != null && localCacheObject.result) {
                    LocalCache.OnlyOnError onlyOnErrorMode = request.getLocalCacheOnlyOnErrorMode();
                    if (onlyOnErrorMode.equals(LocalCache.OnlyOnError.ON_ALL_ERROR) ||
                            (onlyOnErrorMode.equals(LocalCache.OnlyOnError.ON_CONNECTION_ERROR) && result.error instanceof ConnectionException)) {
                        timeStat.tickCacheTime();
                        return localCacheObject.object;
                    }
                }
            }

            if (result.error != null) {
                throw result.error;
            }


            timeStat.tickEndTime();


            if (rpc.isTimeProfiler()) {
                refreshStat(request.getName(),
                        timeStat.getMethodTime(),
                        timeStat.getAllTime()
                );
            }

            if ((rpc.getDebugFlags() & Endpoint.TIME_DEBUG) > 0) {
                timeStat.logTime("End single request(" + request.getName() + "):");
            }

            if ((rpc.isCacheEnabled() && request.isLocalCacheable())) {
                rpc.getMemoryCache().put(request.getMethodId(), request.getArgs(), result.result, request.getLocalCacheSize(), request.getHeaders());
                if (rpc.getCacheMode() == Endpoint.CacheMode.CLONE) {
                    result.result = rpc.getClonner().clone(result.result);
                }
                LocalCache.CacheLevel cacheLevel = request.getLocalCacheLevel();
                if (cacheLevel != LocalCache.CacheLevel.MEMORY_ONLY) {

                    CacheMethod cacheMethod = new CacheMethod(CacheMethod.getMethodId(request.getMethod()), request.getName(), request.getMethod().getDeclaringClass().getSimpleName(), rpc.getUrl(), cacheLevel);
                    rpc.getDiskCache().put(cacheMethod, Arrays.deepToString(request.getArgs()), result.result, request.getLocalCacheSize(), request.getHeaders());
                }
            }

            return result.result;
        } catch (JudoException e) {
            refreshErrorStat(request.getName());
            throw e;
        }

    }

    public List<RequestResult> callBatch(List<RequestImpl> requests, ProgressObserver progressObserver, Integer timeout) throws JudoException {
        final List<RequestResult> results = new ArrayList<>(requests.size());


        if (requests.size() > 0) {


            if (rpc.getProtocolController().isBatchSupported()) {

                List<RequestImpl> copyRequest = new ArrayList<>(requests);
                VirtualServerInfo virtualServerInfo = rpc.getVirtualServers().get(requests.get(0).getMethod().getDeclaringClass());
                if (virtualServerInfo != null) {

                    TimeStat timeStat = new TimeStat(progressObserver);

                    int delay = randDelay(virtualServerInfo.minDelay, virtualServerInfo.maxDelay);

                    for (int i = copyRequest.size() - 1; i >= 0; i--) {
                        RequestImpl request = copyRequest.get(i);

                        VirtualCallback callback = new VirtualCallback(request.getId());
                        Object[] args = request.getArgs() != null ? addElement(request.getArgs(), callback) : new Object[]{callback};
                        boolean implemented = true;
                        try {
                            request.invokeStart(new CacheInfo(false, 0L));
                            try {
                                request.getMethod().invoke(virtualServerInfo.server, args);
                            } catch (IllegalAccessException e) {
                                throw new JudoException("Can't invoke virtual server", e);
                            }

                        } catch (InvocationTargetException ex) {
                            if (ex.getCause() != null && ex.getCause() instanceof UnsupportedOperationException) {
                                implemented = false;
                            } else {
                                throw new JudoException("Can't invoke virtual server", ex);
                            }
                        }
                        if (implemented) {
                            results.add(callback.getResult());
                            copyRequest.remove(request);
                        }
                    }
                    if (copyRequest.size() == 0) {
                        for (int z = 0; z < TimeStat.TICKS; z++) {
                            try {
                                Thread.sleep(delay / TimeStat.TICKS);
                            } catch (InterruptedException e) {
                                throw new JudoException("Thread sleep error", e);
                            }

                            timeStat.tickTime(z);
                        }
                    }
                }
                String requestsName = "";
                for (RequestImpl request : requests) {
                    requestsName += " " + request.getName();
                    findAndCreateBase64(request);
                }
                if (copyRequest.size() > 0) {
                    results.addAll(callRealBatch(copyRequest, progressObserver, timeout, requestsName));
                }
            } else {
                for (RequestImpl request : requests) {
                    findAndCreateBase64(request);
                }
                synchronized (progressObserver) {
                    progressObserver.setMaxProgress(progressObserver.getMaxProgress() + (requests.size() - 1) * TimeStat.TICKS);
                }
                for (final RequestImpl request : requests) {
                    final TimeStat timeStat = new TimeStat(progressObserver);
                    if (!request.isCancelled()) {
                        RequestResult result = sendRequest(request, timeStat);
                        timeStat.tickEndTime();
                        if (rpc.isTimeProfiler()) {
                            if (result.error != null) {
                                refreshErrorStat(request.getName());
                            } else {
                                refreshStat(request.getName(),
                                        timeStat.getMethodTime(),
                                        timeStat.getAllTime()
                                );
                            }
                        }
                        results.add(result);
                    }
                }

            }
        }
        return results;
    }


    private void delay(int requestDelay) {
        int delay = rpc.getDelay() + requestDelay;
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new CancelException();
            }
        }
    }

    public List<RequestResult> callRealBatch(List<RequestImpl> requests, ProgressObserver progressObserver, Integer timeout, String requestsName) throws JudoException {

        try {

            ProtocolController controller = rpc.getProtocolController();
            List<RequestResult> responses;
            TimeStat timeStat = new TimeStat(progressObserver);


            ProtocolController.RequestInfo requestInfo = controller.createRequests(rpc.getUrl(), (List) requests);
            timeStat.tickCreateTime();
            lossCheck();
            int maxDelay = 0;
            for (RequestImpl request : requests) {
                maxDelay = Math.max(maxDelay, request.getDelay());
            }
            EndpointImpl.checkThread();
            delay(maxDelay);
            TransportLayer.Connection conn = transportLayer.send(requestsName, controller, requestInfo, timeout, timeStat, rpc.getDebugFlags(), null);
            EndpointImpl.checkThread();
            InputStream connectionStream = conn.getStream();
            if ((rpc.getDebugFlags() & Endpoint.RESPONSE_DEBUG) > 0) {

                String resStr = FileUtils.convertStreamToString(conn.getStream());
                longLog("Response body(" + requestsName + ", " + resStr.length() + " Bytes)", resStr, JudoLogger.LogLevel.INFO);
                connectionStream = new ByteArrayInputStream(resStr.getBytes());
            }
            EndpointImpl.checkThread();
            RequestInputStream stream = new RequestInputStream(connectionStream, timeStat, conn.getContentLength());
            for (RequestImpl request : requests) {
                request.setHeaders(conn.getHeaders());
            }
            responses = controller.parseResponses((List) requests, stream, conn.getHeaders());
            EndpointImpl.checkThread();
            timeStat.tickParseTime();
            conn.close();
            timeStat.tickEndTime();
            calcTimeProfiler(requests, timeStat);
            if ((rpc.getDebugFlags() & Endpoint.TIME_DEBUG) > 0) {
                timeStat.logTime("End batch request(" + requestsName.substring(1) + "):");
            }

            return responses;
        } catch (JudoException e) {
            for (RequestImpl request : requests) {
                refreshErrorStat(request.getName());
                rpc.saveStat();
            }
            RequestProxy.addToExceptionMessage(requestsName.substring(1), e);
            throw e;
        }
    }

    private void calcTimeProfiler(List<RequestImpl> requests, TimeStat timeStat) {
        if (rpc.isTimeProfiler()) {
            for (RequestImpl request : requests) {
                refreshStat(request.getName(),
                        timeStat.getMethodTime() / requests.size(),
                        timeStat.getAllTime() / requests.size()
                );
            }
            rpc.saveStat();
        }
    }


    private void lossCheck() throws JudoException {
        float percentLoss = rpc.getPercentLoss();
        float random = randomGenerator.nextFloat();
        if (percentLoss != 0 && random < percentLoss) {
            throw new ConnectionException("Random package lost.");
        }
    }

    private MethodStat getStat(String method) {
        MethodStat stat;
        if (rpc.getStats().containsKey(method)) {
            stat = rpc.getStats().get(method);
        } else {
            stat = new MethodStat();
            rpc.getStats().put(method, stat);
        }
        return stat;
    }

    private void refreshStat(String method, long methodTime, long allTime) {
        MethodStat stat = getStat(method);
        stat.methodTime = ((stat.methodTime * stat.requestCount) + methodTime) / (stat.requestCount + 1);
        stat.allTime = ((stat.allTime * stat.requestCount) + allTime) / (stat.requestCount + 1);
        stat.requestCount++;
        rpc.saveStat();
    }

    private void refreshErrorStat(String method) {
        MethodStat stat = getStat(method);
        stat.errors++;
        stat.requestCount++;
        rpc.saveStat();
    }

    public void setConnectTimeout(int connectTimeout) {
        transportLayer.setConnectTimeout(connectTimeout);
    }

    public void setMethodTimeout(int methodTimeout) {
        transportLayer.setMethodTimeout(methodTimeout);
    }

    public int getMethodTimeout() {
        return transportLayer.getMethodTimeout();
    }

    public int randDelay(int minDelay, int maxDelay) {
        if (maxDelay == 0) {
            return 0;
        }
        Random random = new Random();
        if (maxDelay != minDelay) {
            return minDelay + random.nextInt(maxDelay - minDelay);
        } else {
            return minDelay;
        }
    }
}
