package com.github.kubatatami.judonetworking.internals;


import android.util.Base64;

import com.github.kubatatami.judonetworking.CacheInfo;
import com.github.kubatatami.judonetworking.Endpoint;
import com.github.kubatatami.judonetworking.annotations.Base64Param;
import com.github.kubatatami.judonetworking.annotations.LocalCache;
import com.github.kubatatami.judonetworking.annotations.ProcessingMethod;
import com.github.kubatatami.judonetworking.annotations.Required;
import com.github.kubatatami.judonetworking.annotations.RequiredList;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.exceptions.AuthException;
import com.github.kubatatami.judonetworking.exceptions.CancelException;
import com.github.kubatatami.judonetworking.exceptions.ConnectionException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.exceptions.VerifyModelException;
import com.github.kubatatami.judonetworking.internals.cache.CacheMethod;
import com.github.kubatatami.judonetworking.internals.requests.RequestImpl;
import com.github.kubatatami.judonetworking.internals.results.CacheResult;
import com.github.kubatatami.judonetworking.internals.results.ErrorResult;
import com.github.kubatatami.judonetworking.internals.results.NoNewResult;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;
import com.github.kubatatami.judonetworking.internals.results.RequestSuccessResult;
import com.github.kubatatami.judonetworking.internals.stats.MethodStat;
import com.github.kubatatami.judonetworking.internals.stats.TimeStat;
import com.github.kubatatami.judonetworking.internals.streams.RequestInputStream;
import com.github.kubatatami.judonetworking.internals.virtuals.VirtualCallback;
import com.github.kubatatami.judonetworking.internals.virtuals.VirtualServerInfo;
import com.github.kubatatami.judonetworking.logs.JudoLogger;
import com.github.kubatatami.judonetworking.transports.TransportLayer;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

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

    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private RequestResult sendRequest(RequestImpl request, TimeStat timeStat) {
        return sendRequest(request, timeStat, null, null, false);
    }

    private RequestResult sendRequest(RequestImpl request, TimeStat timeStat, String hash, Long time) {
        return sendRequest(request, timeStat, hash, time, false);
    }

    private RequestResult sendRequest(RequestImpl request, TimeStat timeStat, String hash, Long time, boolean ignoreTokenError) {
        try {
            RequestResult result;
            long currentTokenExpireTimestamp;
            ProtocolController controller = rpc.getProtocolController();
            Object virtualObject = handleVirtualServerRequest(request, timeStat);
            if (virtualObject != null) {
                currentTokenExpireTimestamp = 0;
                result = new RequestSuccessResult(request.getId(), virtualObject);
            } else {
                ProtocolController.RequestInfo requestInfo = controller.createRequest(
                        request.getCustomUrl() == null ? rpc.getUrl() : request.getCustomUrl(),
                        request);
                timeStat.tickCreateTime();
                lossCheck();
                EndpointImpl.checkThread();
                delay(request.getDelay());
                currentTokenExpireTimestamp = rpc.getTokenExpireTimestamp();
                if (rpc.getTokenCaller() != null && request.isApiKeyRequired() && !ignoreTokenError && !checkTokenExpireTimestamp(currentTokenExpireTimestamp)) {
                    try {
                        doTokenRequest(currentTokenExpireTimestamp);
                    } catch (Exception ex) {
                        return new ErrorResult(request.getId(), new AuthException("Can't obtain api token", ex));
                    }
                }
                TransportLayer.Connection conn = transportLayer.send(request.getName(), controller, requestInfo, request.getTimeout(), timeStat,
                        rpc.getDebugFlags(), request.getMethod(), new TransportLayer.CacheInfo(hash, time));
                EndpointImpl.checkThread();
                if (!conn.isNewestAvailable()) {
                    if ((rpc.getDebugFlags() & Endpoint.RESPONSE_DEBUG) > 0) {
                        JudoLogger.log("No new data for method " + request.getName(), JudoLogger.LogLevel.DEBUG);
                    }

                    return new NoNewResult();
                }

                InputStream connectionStream = conn.getStream();
                if ((rpc.getDebugFlags() & Endpoint.RESPONSE_DEBUG) > 0) {

                    String resStr = convertStreamToString(conn.getStream());
                    longLog("Response body(" + request.getName() + ", " + resStr.length() + " Bytes)", resStr, JudoLogger.LogLevel.INFO);
                    connectionStream = new ByteArrayInputStream(resStr.getBytes());
                }
                RequestInputStream stream = new RequestInputStream(connectionStream, timeStat, conn.getContentLength());
                EndpointImpl.checkThread();
                result = controller.parseResponse(request, stream, conn.getHeaders());
                EndpointImpl.checkThread();
                if (result instanceof RequestSuccessResult) {
                    result.hash = conn.getHash();
                    result.time = conn.getDate();
                }
                try {
                    stream.close();
                } catch (Exception ignored) {
                }
                timeStat.tickParseTime();
                conn.close();
            }
            if (request.isApiKeyRequired() && result.error != null &&
                    rpc.getTokenCaller() != null && !ignoreTokenError &&
                    rpc.getTokenCaller().checkIsTokenException(result.error)) {
                try {
                    doTokenRequest(currentTokenExpireTimestamp);
                    request.clearProgress();
                    request.setAdditionalControllerData(rpc.getProtocolController().getAdditionalRequestData());
                    return sendRequest(request, timeStat, hash, time, true);
                } catch (Exception ex) {
                    return new ErrorResult(request.getId(), new AuthException("Can't obtain api token", ex));
                }
            } else if (result instanceof RequestSuccessResult) {
                if (rpc.isVerifyResultModel()) {
                    verifyResult(request, result);
                }
                if (rpc.isProcessingMethod()) {
                    processingMethod(result.result);
                }
            }
            return result;
        } catch (JudoException e) {
            return new ErrorResult(request.getId(), e);
        } catch (Exception e) {
            return new ErrorResult(request.getId(), new JudoException(e));
        }

    }

    protected boolean checkTokenExpireTimestamp(long tokenExpireTimestamp) {
        return (tokenExpireTimestamp == 0 || (tokenExpireTimestamp != -1 && tokenExpireTimestamp > System.currentTimeMillis()));
    }

    protected void doTokenRequest(long oldTokenExpireTimestamp) throws Exception {
        synchronized (rpc.getTokenCaller()) {
            if (oldTokenExpireTimestamp == rpc.getTokenExpireTimestamp()) {
                long tokenExpireTimestamp = rpc.getTokenCaller().doTokenRequest(rpc);
                rpc.setTokenExpireTimestamp(tokenExpireTimestamp);
            }
        }
    }

    public static void processingMethod(Object object) {
        if (object instanceof Iterable) {
            for (Object obj : ((Iterable) object)) {
                processingMethod(obj);
            }
        } else {
            for (Field field : object.getClass().getFields()) {
                field.setAccessible(true);
                try {
                    if (!field.getDeclaringClass().equals(Object.class) && !field.getType().isPrimitive()) {
                        Object fieldObject = field.get(object);
                        if (fieldObject != null) {
                            processingMethod(fieldObject);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            invokeProcessingMethod(object);
        }
    }

    public static void invokeProcessingMethod(Object result) {
        Class<?> clazz = result.getClass();
        do {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(ProcessingMethod.class)) {
                    method.setAccessible(true);
                    try {
                        method.invoke(result);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Object.class));

    }

    public static void verifyResult(RequestImpl request, RequestResult result) throws VerifyModelException {
        if (result instanceof RequestSuccessResult && !request.isVoidResult()) {


            if (result.result == null) {
                Required ann = ReflectionCache.getAnnotation(request.getMethod(), Required.class);
                if (ann != null) {
                    throw new VerifyModelException("Result object required.");
                }
                RequiredList ann2 = ReflectionCache.getAnnotation(request.getMethod(), RequiredList.class);
                if (ann2 != null) {
                    throw new VerifyModelException("Result object required.");
                }
            } else {
                RequiredList ann = ReflectionCache.getAnnotation(request.getMethod(), RequiredList.class);
                if (ann != null) {
                    if (result.result instanceof Iterable) {
                        int i = 0;
                        for (Object obj : (Iterable) result.result) {
                            verifyResultObject(obj);
                            i++;
                        }
                        if (ann.minSize() > 0 && i < ann.minSize()) {
                            throw new VerifyModelException("Result list from method " + request.getName() + "(size " + i + ") is smaller then limit: " + ann.minSize() + ".");
                        }
                        if (ann.maxSize() > 0 && i > ann.maxSize()) {
                            throw new VerifyModelException("Result list from method " + request.getName() + "(size " + i + ") is larger then limit: " + ann.maxSize() + ".");
                        }
                    }
                }
                verifyResultObject(result.result);
            }
        }
    }

    public static void verifyResultObject(Object object) throws VerifyModelException {
        if (object instanceof Iterable) {
            for (Object obj : ((Iterable) object)) {
                verifyResultObject(obj);
            }
        } else {
            for (Field field : object.getClass().getFields()) {
                try {
                    field.setAccessible(true);

                    if (field.get(object) == null) {
                        if (field.isAnnotationPresent(Required.class) || field.isAnnotationPresent(RequiredList.class)) {
                            throw new VerifyModelException("Field " + object.getClass().getName() + "." + field.getName() + " required.");
                        }
                    } else {

                        Object iterableObject = field.get(object);
                        if (iterableObject instanceof Iterable) {
                            RequiredList ann = field.getAnnotation(RequiredList.class);
                            if (ann != null) {
                                int i = 0;
                                for (Object obj : (Iterable) iterableObject) {
                                    verifyResultObject(obj);
                                    i++;
                                }

                                if (ann.minSize() > 0 && i < ann.minSize()) {
                                    throw new VerifyModelException("List " + object.getClass().getName() + "." + field.getName() + "(size " + i + ") is smaller then limit: " + ann.minSize() + ".");
                                }
                                if (ann.maxSize() > 0 && i > ann.maxSize()) {
                                    throw new VerifyModelException("List " + object.getClass().getName() + "." + field.getName() + "(size " + i + ") is larger then limit: " + ann.maxSize() + ".");
                                }
                            }
                        } else if (field.getAnnotation(Required.class) != null) {
                            verifyResultObject(field.get(object));
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Object[] addElement(Object[] org, Object added) {
        Object[] result = new Object[org.length + 1];
        System.arraycopy(org, 0, result, 0, org.length);
        result[org.length] = added;
        return result;
    }


    private Object handleVirtualServerRequest(RequestImpl request, TimeStat timeStat) throws JudoException {
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
                            return object;
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
                            request.getMethod().invoke(virtualServerInfo.server, args);
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
                            if (callback.getResult().error != null) {
                                throw callback.getResult().error;
                            }
                            return callback.getResult().result;
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
            CacheResult serverCacheObject = null;
            TimeStat timeStat = new TimeStat(request);


            if ((rpc.isCacheEnabled() && request.isLocalCacheable()) || rpc.isTest()) {
                LocalCache.CacheLevel cacheLevel = rpc.isTest() ? LocalCache.CacheLevel.DISK_CACHE : request.getLocalCacheLevel();
                localCacheObject = rpc.getMemoryCache().get(request.getMethodId(), request.getArgs(), rpc.isTest() ? 0 : request.getLocalCacheLifeTime(), request.getLocalCacheSize());
                if (localCacheObject.result) {
                    if (request.getLocalCacheOnlyOnErrorMode().equals(LocalCache.OnlyOnError.NO)) {
                        request.invokeStart(new CacheInfo(true, localCacheObject.time));
                        timeStat.tickCacheTime();
                        return localCacheObject.object;
                    }
                } else if (cacheLevel != LocalCache.CacheLevel.MEMORY_ONLY) {
                    CacheMethod cacheMethod = new CacheMethod(CacheMethod.getMethodId(request.getMethod()), request.getName(), request.getMethod().getDeclaringClass().getSimpleName(), rpc.getTestName(), rpc.getTestRevision(), rpc.getUrl(), cacheLevel);
                    localCacheObject = rpc.getDiskCache().get(cacheMethod, Arrays.deepToString(request.getArgs()), request.getLocalCacheLifeTime());
                    if (localCacheObject.result) {
                        if (!rpc.isTest()) {  //we don't know when test will be stop
                            rpc.getMemoryCache().put(request.getMethodId(), request.getArgs(), localCacheObject.object, request.getLocalCacheSize());
                        }
                        if (request.getLocalCacheOnlyOnErrorMode().equals(LocalCache.OnlyOnError.NO)) {
                            request.invokeStart(new CacheInfo(true, localCacheObject.time));
                            timeStat.tickCacheTime();
                            return localCacheObject.object;
                        }
                    }

                }
            }

            if (rpc.isCacheEnabled() && request.isServerCacheable()) {
                CacheMethod cacheMethod = new CacheMethod(CacheMethod.getMethodId(request.getMethod()), request.getName(), request.getMethod().getDeclaringClass().getSimpleName(), rpc.getUrl(), request.getServerCacheLevel());
                serverCacheObject = rpc.getDiskCache().get(cacheMethod, Arrays.deepToString(request.getArgs()), 0);

            }

            findAndCreateBase64(request);
            request.invokeStart(new CacheInfo(false, 0L));
            RequestResult result;
            if (serverCacheObject != null && serverCacheObject.result) {
                result = sendRequest(request, timeStat, serverCacheObject.hash, serverCacheObject.time);
                if (result instanceof NoNewResult) {
                    return serverCacheObject.object;
                } else if (result instanceof ErrorResult && request.useServerCacheOldOnError()) {
                    return serverCacheObject.object;
                }
            } else {
                result = sendRequest(request, timeStat, null, null);
            }

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
                refreshStat(request.getName(), timeStat.getMethodTime());
            }

            if ((rpc.getDebugFlags() & Endpoint.TIME_DEBUG) > 0) {
                timeStat.logTime("End single request(" + request.getName() + "):");
            }

            if ((rpc.isCacheEnabled() && request.isLocalCacheable()) || rpc.isTest()) {
                rpc.getMemoryCache().put(request.getMethodId(), request.getArgs(), result.result, request.getLocalCacheSize());
                if (rpc.getCacheMode() == Endpoint.CacheMode.CLONE) {
                    result.result = rpc.getClonner().clone(result.result);
                }
                LocalCache.CacheLevel cacheLevel = rpc.isTest() ? LocalCache.CacheLevel.DISK_CACHE : request.getLocalCacheLevel();
                if (cacheLevel != LocalCache.CacheLevel.MEMORY_ONLY) {

                    CacheMethod cacheMethod = new CacheMethod(CacheMethod.getMethodId(request.getMethod()), request.getName(), request.getMethod().getDeclaringClass().getSimpleName(), rpc.getTestName(), rpc.getTestRevision(), rpc.getUrl(), cacheLevel);
                    rpc.getDiskCache().put(cacheMethod, Arrays.deepToString(request.getArgs()), result.result, request.getLocalCacheSize());
                }


            } else if (rpc.isCacheEnabled() && request.isServerCacheable() && (result.hash != null || result.time != null)) {
                CacheMethod cacheMethod = new CacheMethod(CacheMethod.getMethodId(request.getMethod()), request.getName(), request.getMethod().getDeclaringClass().getSimpleName(), rpc.getUrl(), request.getServerCacheLevel());
                rpc.getDiskCache().put(cacheMethod, Arrays.deepToString(request.getArgs()), result.result, request.getServerCacheSize());
            }


            return result.result;
        } catch (JudoException e) {
            refreshErrorStat(request.getName(), request.getTimeout());
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

                    CacheResult cacheObject = null;
                    if (!request.isCancelled()) {


                        if (rpc.isCacheEnabled() && request.isServerCacheable()) {
                            CacheMethod cacheMethod = new CacheMethod(CacheMethod.getMethodId(request.getMethod()), request.getName(), request.getMethod().getDeclaringClass().getSimpleName(), rpc.getUrl(), request.getServerCacheLevel());
                            cacheObject = rpc.getDiskCache().get(cacheMethod, Arrays.deepToString(request.getArgs()), request.getServerCacheSize());

                        }

                        if (cacheObject != null && cacheObject.result) {
                            RequestResult result = sendRequest(request, timeStat, cacheObject.hash, cacheObject.time);

                            if (result instanceof NoNewResult) {
                                results.add(new RequestSuccessResult(request.getId(), cacheObject.object));
                            } else if (result instanceof ErrorResult && request.useServerCacheOldOnError()) {
                                results.add(new RequestSuccessResult(request.getId(), cacheObject.object));
                            } else {
                                results.add(result);
                            }
                        } else {
                            RequestResult result = sendRequest(request, timeStat);
                            results.add(result);
                        }
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
            boolean isApiRequired = false;
            for (RequestImpl request : requests) {
                if (request.isApiKeyRequired()) {
                    isApiRequired = true;
                }
            }
            long currentTokenExpireTimestamp = rpc.getTokenExpireTimestamp();
            if (rpc.getTokenCaller() != null && isApiRequired && !checkTokenExpireTimestamp(currentTokenExpireTimestamp)) {
                try {
                    doTokenRequest(currentTokenExpireTimestamp);
                } catch (Exception ex) {
                    throw new AuthException("Can't obtain api token", ex);
                }
            }
            TransportLayer.Connection conn = transportLayer.send(requestsName, controller, requestInfo, timeout, timeStat, rpc.getDebugFlags(), null, null);
            EndpointImpl.checkThread();
            InputStream connectionStream = conn.getStream();
            if ((rpc.getDebugFlags() & Endpoint.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(conn.getStream());
                longLog("Response body(" + requestsName + ", " + resStr.length() + " Bytes)", resStr, JudoLogger.LogLevel.INFO);
                connectionStream = new ByteArrayInputStream(resStr.getBytes());
            }
            EndpointImpl.checkThread();
            RequestInputStream stream = new RequestInputStream(connectionStream, timeStat, conn.getContentLength());
            responses = controller.parseResponses((List) requests, stream, conn.getHeaders());
            EndpointImpl.checkThread();
            timeStat.tickParseTime();
            conn.close();
            timeStat.tickEndTime();
            if (rpc.isTimeProfiler()) {

                for (RequestImpl request : requests) {
                    refreshStat(request.getName(), timeStat.getMethodTime() / requests.size());
                }
                rpc.saveStat();
            }


            if ((rpc.getDebugFlags() & Endpoint.TIME_DEBUG) > 0) {
                timeStat.logTime("End batch request(" + requestsName.substring(1) + "):");
            }

            return responses;
        } catch (JudoException e) {
            for (RequestImpl request : requests) {
                refreshErrorStat(request.getName(), request.getTimeout());
                rpc.saveStat();
            }
            RequestProxy.addToExceptionMessage(requestsName.substring(1), e);
            throw e;
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

    private void refreshStat(String method, long time) {
        MethodStat stat = getStat(method);
        stat.avgTime = ((stat.avgTime * stat.requestCount) + time) / (stat.requestCount + 1);
        stat.requestCount++;
        rpc.saveStat();
    }

    private void refreshErrorStat(String method, long timeout) {
        MethodStat stat = getStat(method);
        stat.avgTime = ((stat.avgTime * stat.requestCount) + timeout) / (stat.requestCount + 1);
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
