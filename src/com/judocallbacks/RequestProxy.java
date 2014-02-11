package com.judocallbacks;

import android.util.Pair;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

class RequestProxy implements InvocationHandler {

    private final EndpointImplementation rpc;
    private int id = 0;
    private boolean batch = false;
    private boolean batchFatal = true;
    private final List<Request> batchRequests = new ArrayList<Request>();
    private BatchMode mode = BatchMode.NONE;

    public RequestProxy(EndpointImplementation rpc, BatchMode mode) {
        this.rpc = rpc;
        this.mode = mode;
        if (mode == BatchMode.MANUAL) {
            batch = true;
        }
    }


    public void setBatchFatal(boolean batchFatal) {
        this.batchFatal = batchFatal;
    }

    private Method getMethod(Object obj, String name) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    public static String getMethodName(Method method) {
        RequestMethod ann = method.getAnnotation(RequestMethod.class);
        return (ann != null && !ann.name().equals("")) ? ann.name() : method.getName();
    }

    public static StackTraceElement getExternalStacktrace(StackTraceElement[] stackTrace) {
        String packageName = RequestProxy.class.getPackage().getName();
        boolean current = false;
        for (StackTraceElement element : stackTrace) {
            if (!current && element.getClassName().contains(packageName)) {
                current = true;
            } else if (current && !element.getClassName().contains(packageName) && !element.getClassName().contains("$Proxy")) {
                return element;
            }
        }
        return stackTrace[0];
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        try {
            Method method = getMethod(this, m.getName());
            RequestMethod ann = m.getAnnotation(RequestMethod.class);
            if (method != null) {
                return method.invoke(this, args);
            } else if (ann != null) {
                String name = getMethodName(m);
                int timeout = rpc.getRequestConnector().getMethodTimeout();

                if ((rpc.getDebugFlags() & Endpoint.REQUEST_LINE_DEBUG) > 0) {
                    StackTraceElement stackTraceElement = getExternalStacktrace(Thread.currentThread().getStackTrace());
                    if (batch && mode == BatchMode.MANUAL) {
                        LoggerImpl.log("Batch request " + name + " from " +
                                stackTraceElement.getClassName() +
                                "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
                    } else {
                        LoggerImpl.log("Request " + name + " from " +
                                stackTraceElement.getClassName() +
                                "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
                    }
                }

                if (ann.timeout() != 0) {
                    timeout = ann.timeout();
                }

                if (!ann.async()) {
                    return rpc.getRequestConnector().call(new Request(++id, rpc, m, name, ann, args, m.getReturnType(), timeout, null,rpc.getProtocolController().getAdditionalRequestData()));
                } else {
                    final Request request = callAsync(++id, m, name, args, m.getGenericParameterTypes(), timeout, ann);

                    if (batch) {
                        synchronized (batchRequests) {
                            request.setBatchFatal(batchFatal);
                            batchRequests.add(request);
                            batch = true;
                        }
                        return null;
                    } else {

                        if (mode == BatchMode.AUTO) {

                            synchronized (batchRequests) {
                                batchRequests.add(request);
                                batch = true;
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                                    try {
                                        Thread.sleep(rpc.getAutoBatchTime());
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    RequestProxy.this.callBatch(null);
                                }
                            }).start();

                            return null;
                        } else {


                            Thread thread = new Thread(request);
                            thread.start();

                            if (m.getReturnType().equals(Thread.class)) {
                                return thread;
                            } else {
                                return null;
                            }

                        }


                    }

                }
            } else {
                throw new RequestException("No @RequestMethod on " + m.getName());
            }
        } catch (Exception e) {
            if (rpc.getErrorLogger() != null) {
                rpc.getErrorLogger().onError(e);
            }
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    Request callAsync(int id, Method m, String name, Object[] args, Type[] types, int timeout, RequestMethod ann) throws Exception {
        Object[] newArgs = args;
        CallbackInterface<Object> callback = null;
        Type returnType = Void.class;
        if(args[args.length - 1] instanceof CallbackInterface){
            callback=(CallbackInterface<Object>) args[args.length - 1];
            returnType = ((ParameterizedType) types[args.length - 1]).getActualTypeArguments()[0];
            if (args.length > 1) {
                newArgs = new Object[args.length - 1];
                System.arraycopy(args, 0, newArgs, 0, args.length - 1);
            }else{
                newArgs = null;
            }
        }


        return new Request(id, rpc, m, name, ann, newArgs, returnType, timeout, callback,rpc.getProtocolController().getAdditionalRequestData());
    }


    public void callBatch(final Batch batch) {
        List<Request> batches;
        if (batchRequests.size() > 0) {


            synchronized (batchRequests) {
                batches = new ArrayList<Request>(batchRequests.size());
                batches.addAll(batchRequests);
                batchRequests.clear();
                this.batch = false;
            }

            Map<Integer, Pair<Request, Object>> cacheObjects = new HashMap<Integer, Pair<Request, Object>>();
            if (rpc.isCacheEnabled()) {
                for (int i = batches.size() - 1; i >= 0; i--) {
                    Request req = batches.get(i);

                    if (req.isLocalCachable() || rpc.isTest()) {
                        CacheResult result = rpc.getMemoryCache().get(req.getMethod(), req.getArgs(), rpc.isTest() ? 0 : req.getLocalCacheLifeTime(), req.getLocalCacheSize());
                        LocalCacheLevel cacheLevel = rpc.isTest() ? LocalCacheLevel.DISK_CACHE : req.getLocalCacheLevel();
                        if (result.result) {
                            if (rpc.getCacheMode() == CacheMode.CLONE) {
                                try {
                                    result.object = rpc.getClonner().clone(result.object);
                                    cacheObjects.put(req.getId(), new Pair<Request, Object>(req, result.object));
                                    if (!req.isLocalCacheOnlyOnError()) {
                                        batches.remove(i);
                                    }
                                } catch (Exception e) {
                                    LoggerImpl.log(e);
                                }
                            }


                        } else if (cacheLevel != LocalCacheLevel.MEMORY_ONLY) {
                            CacheMethod cacheMethod = new CacheMethod(rpc.getTestName(), rpc.getTestRevision(), rpc.getUrl(), req.getMethod(), cacheLevel);
                            result = rpc.getDiskCache().get(cacheMethod, Arrays.deepToString(req.getArgs()), req.getLocalCacheLifeTime());
                            if (result.result) {
                                if (!rpc.isTest()) {
                                    rpc.getMemoryCache().put(req.getMethod(), req.getArgs(), result.object, req.getLocalCacheSize());
                                }
                                cacheObjects.put(req.getId(), new Pair<Request, Object>(req, result.object));
                                if (!req.isLocalCacheOnlyOnError()) {
                                    batches.remove(i);
                                }
                            }

                        }
                    }
                }
            }

            BatchProgressObserver batchProgressObserver = new BatchProgressObserver(rpc, batch, batches);
            List<RequestResult> responses;
            if (batches.size() > 0) {
                try {
                    responses = sendBatchRequest(batches, batchProgressObserver, cacheObjects.size());
                } catch (final Exception e) {
                    responses = new ArrayList<RequestResult>(batches.size());
                    for (Request request : batches) {
                        responses.add(new ErrorResult(request.getId(), e));
                    }
                }
                Collections.sort(responses);
            } else {
                responses = new ArrayList<RequestResult>();
                batchProgressObserver.setMaxProgress(1);
                batchProgressObserver.progressTick(1);
            }

            if (rpc.isCacheEnabled()) {
                for (int i = responses.size() - 1; i >= 0; i--) {
                    RequestResult result = responses.get(i);
                    if (cacheObjects.containsKey(result.id) && result instanceof ErrorResult) {
                        responses.remove(result);
                        RequestSuccessResult res = new RequestSuccessResult(cacheObjects.get(result.id).second);
                        res.id=result.id;
                        responses.add(res);
                        cacheObjects.remove(result.id);
                    }
                    else{
                        cacheObjects.remove(result.id);
                    }
                }

                for (Map.Entry<Integer, Pair<Request, Object>> pairs : cacheObjects.entrySet()) {
                    RequestSuccessResult res = new RequestSuccessResult(cacheObjects.get(pairs.getKey()).second);
                    res.id=pairs.getKey();
                    responses.add(res);
                    batches.add(pairs.getKey(), pairs.getValue().first);
                }
            }
            Collections.sort(batches, new Comparator<Request>() {
                @Override
                public int compare(Request lhs, Request rhs) {
                    return lhs.getId().compareTo(rhs.getId());
                }
            });
            Collections.sort(responses, new Comparator<RequestResult>() {
                @Override
                public int compare(RequestResult lhs, RequestResult rhs) {
                    return lhs.id.compareTo(rhs.id);
                }
            });
            handleBatchResponse(batches, batch, responses);


        } else {
            this.batch = false;
            if (batch != null) {
                batch.onFinish(new Object[]{});
            }
        }
    }

    private List<List<Request>> assignRequestsToConnections(List<Request> list, final int partsNo) {
        if (rpc.isTimeProfiler()) {
            return BatchTask.timeAssignRequests(list, partsNo);
        } else {
            return BatchTask.simpleAssignRequests(list, partsNo);
        }
    }


    private int calculateTimeout(List<Request> batches) {
        int timeout = 0;
        if (rpc.getTimeoutMode() == BatchTimeoutMode.TIMEOUTS_SUM) {
            for (Request req : batches) {
                timeout += req.getTimeout();
            }
        } else {
            for (Request req : batches) {
                timeout = Math.max(timeout, req.getTimeout());
            }
        }
        return timeout;
    }

    private List<RequestResult> sendBatchRequest(List<Request> batches, BatchProgressObserver progressObserver, int cachedRequests) throws Exception {
        int conn = rpc.getMaxConnections();

        if (batches.size() > 1 && conn > 1) {
            int connections = Math.min(batches.size(), conn);
            List<List<Request>> requestParts = assignRequestsToConnections(batches, connections);
            List<RequestResult> response = new ArrayList<RequestResult>(batches.size());
            List<BatchTask> tasks = new ArrayList<BatchTask>(connections);

            progressObserver.setMaxProgress((requestParts.size() + cachedRequests) * TimeStat.TICKS);
            if (cachedRequests > 0) {
                progressObserver.progressTick(cachedRequests * TimeStat.TICKS);
            }

            for (List<Request> requests : requestParts) {

                BatchTask task = new BatchTask(rpc, progressObserver, calculateTimeout(requests), requests);
                tasks.add(task);
            }
            for (BatchTask task : tasks) {
                task.execute();
            }
            for (BatchTask task : tasks) {
                task.join();
                if (task.getEx() != null) {
                    throw task.getEx();
                } else {
                    response.addAll(task.getResponse());
                }
            }
            return response;
        } else {
            progressObserver.setMaxProgress(TimeStat.TICKS);
            return rpc.getRequestConnector().callBatch(batches, progressObserver, calculateTimeout(batches));
        }
    }

    private void handleBatchResponse(List<Request> requests, Batch batch, List<RequestResult> responses) {
        Object[] results = new Object[requests.size()];
        Exception ex = null;
        int i = 0;
        for (Request request : requests) {
            try {
                RequestResult response = responses.get(i);

                if (response.cacheObject != null) {
                    results[i] = response.cacheObject;
                } else {


                    if (response.error != null) {
                        throw response.error;
                    }

                    if (request.getReturnType() != Void.class) {
                        if (rpc.isVerifyResultModel()) {
                            RequestConnector.verifyResult(request, response);
                        }
                        if (rpc.isProcessingMethod()) {
                            RequestConnector.processingMethod(response.result);
                        }
                        results[i] = response.result;
                        if ((rpc.isCacheEnabled() && request.isLocalCachable()) || rpc.isTest()) {
                            rpc.getMemoryCache().put(request.getMethod(), request.getArgs(), results[i], request.getLocalCacheSize());
                            if (rpc.getCacheMode() == CacheMode.CLONE) {
                                results[i] = rpc.getClonner().clone(results[i]);
                            }
                            LocalCacheLevel cacheLevel = rpc.isTest() ? LocalCacheLevel.DISK_CACHE : request.getLocalCacheLevel();

                            if (cacheLevel != LocalCacheLevel.MEMORY_ONLY) {
                                CacheMethod cacheMethod = new CacheMethod(rpc.getTestName(), rpc.getTestRevision(), rpc.getUrl(), request.getMethod(), cacheLevel);
                                rpc.getDiskCache().put(cacheMethod, Arrays.deepToString(request.getArgs()), results[i], request.getLocalCacheSize());
                            }
                        } else if (rpc.isCacheEnabled() && request.isServerCachable() && (response.hash != null || response.time != null)) {
                            CacheMethod cacheMethod = new CacheMethod(rpc.getUrl(), request.getMethod(), response.hash, response.time, request.getServerCacheLevel());
                            rpc.getDiskCache().put(cacheMethod, Arrays.deepToString(request.getArgs()), results[i], request.getServerCacheSize());
                        }
                    }
                }
                request.invokeCallback(results[i]);
            } catch (Exception e) {
                if (request.isBatchFatal()) {
                    ex = e;
                }
                request.invokeCallbackException(new RequestException(request.getName(), e));
            }
            i++;
        }
        if (batch != null) {
            if (ex == null) {
                Request.invokeBatchCallback(rpc, batch, results);
            } else {
                Request.invokeBatchCallback(rpc, batch, ex);
            }
        }
        if (ex != null) {
            final Exception finalEx = ex;
            if (rpc.getErrorLogger() != null) {
                rpc.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        rpc.getErrorLogger().onError(finalEx);
                    }
                });

            }
        }
    }


}
