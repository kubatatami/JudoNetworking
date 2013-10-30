package com.jsonrpclib;

import android.util.Pair;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

class JsonProxy implements InvocationHandler {

    private final JsonRpcImplementation rpc;
    private int id = 0;
    private boolean batch = false;
    private boolean batchFatal = true;
    private final List<JsonRequest> batchRequests = new ArrayList<JsonRequest>();
    private JsonBatchMode mode = JsonBatchMode.NONE;

    public JsonProxy(JsonRpcImplementation rpc, JsonBatchMode mode) {
        this.rpc = rpc;
        this.mode = mode;
        if (mode == JsonBatchMode.MANUAL) {
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
        JsonMethod ann = method.getAnnotation(JsonMethod.class);
        return (ann != null && !ann.name().equals("")) ? ann.name() : method.getName();
    }

    public static StackTraceElement getExternalStacktrace(StackTraceElement[] stackTrace) {
        String packageName = JsonProxy.class.getPackage().getName();
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
            JsonMethod ann = m.getAnnotation(JsonMethod.class);
            if (method != null) {
                return method.invoke(this, args);
            } else if (ann != null) {
                String name = getMethodName(m);
                int timeout = rpc.getJsonConnector().getMethodTimeout();

                if ((rpc.getDebugFlags() & JsonRpc.REQUEST_LINE_DEBUG) > 0) {
                    StackTraceElement stackTraceElement = getExternalStacktrace(Thread.currentThread().getStackTrace());
                    if (batch && mode == JsonBatchMode.MANUAL) {
                        JsonLoggerImpl.log("Batch request " + name + " from " +
                                stackTraceElement.getClassName() +
                                "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
                    } else {
                        JsonLoggerImpl.log("Request " + name + " from " +
                                stackTraceElement.getClassName() +
                                "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
                    }
                }

                if (ann.timeout() != 0) {
                    timeout = ann.timeout();
                }

                if (!ann.async()) {
                    return rpc.getJsonConnector().call(new JsonRequest(++id, rpc, m, name, ann, args, m.getReturnType(), timeout, null));
                } else {
                    final JsonRequest request = callAsync(++id, m, name, args, m.getGenericParameterTypes(), timeout, ann);

                    if (batch) {
                        synchronized (batchRequests) {
                            request.setBatchFatal(batchFatal);
                            batchRequests.add(request);
                            batch = true;
                        }
                        return null;
                    } else {

                        if (mode == JsonBatchMode.AUTO) {

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
                                    JsonProxy.this.callBatch(null);
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
                throw new JsonException("No @JsonMethod on " + m.getName());
            }
        } catch (Exception e) {
            if (rpc.getErrorLogger() != null) {
                rpc.getErrorLogger().onError(e);
            }
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    JsonRequest callAsync(int id, Method m, String name, Object[] args, Type[] types, int timeout, JsonMethod ann) throws Exception {
        Object[] newArgs = null;
        JsonCallbackInterface<Object> callback = (JsonCallbackInterface<Object>) args[args.length - 1];
        if (args.length > 1) {
            newArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, newArgs, 0, args.length - 1);
        }
        Type returnType = ((ParameterizedType) types[args.length - 1]).getActualTypeArguments()[0];
        return new JsonRequest(id, rpc, m, name, ann, newArgs, returnType, timeout, callback);
    }


    public void callBatch(final JsonBatch batch) {
        List<JsonRequest> batches;
        if (batchRequests.size() > 0) {


            synchronized (batchRequests) {
                batches = new ArrayList<JsonRequest>(batchRequests.size());
                batches.addAll(batchRequests);
                batchRequests.clear();
                this.batch = false;
            }

            Map<Integer, Pair<JsonRequest, Object>> cacheObjects = new HashMap<Integer, Pair<JsonRequest, Object>>();
            if (rpc.isCacheEnabled()) {
                for (int i = batches.size() - 1; i >= 0; i--) {
                    JsonRequest req = batches.get(i);

                    if (req.isLocalCachable() || rpc.isTest()) {
                        JsonCacheResult result = rpc.getMemoryCache().get(req.getMethod(), req.getArgs(), rpc.isTest() ? 0 : req.getLocalCacheLifeTime(), req.getLocalCacheSize());
                        JsonLocalCacheLevel cacheLevel = rpc.isTest() ? JsonLocalCacheLevel.DISK_CACHE : req.getLocalCacheLevel();
                        if (result.result) {
                            if (rpc.getCacheMode() == JsonCacheMode.CLONE) {
                                try {
                                    result.object = rpc.getJsonClonner().clone(result.object);
                                    cacheObjects.put(i, new Pair<JsonRequest, Object>(req, result.object));
                                    if (req.isLocalCacheOnlyOnError()) {
                                        batches.remove(i);
                                    }
                                } catch (Exception e) {
                                    JsonLoggerImpl.log(e);
                                }
                            }


                        } else if (cacheLevel != JsonLocalCacheLevel.MEMORY_ONLY) {
                            JsonCacheMethod cacheMethod = new JsonCacheMethod(rpc.getTestName(), rpc.getTestRevision(), rpc.getUrl(), req.getMethod(), cacheLevel);
                            result = rpc.getDiskCache().get(cacheMethod, Arrays.deepToString(req.getArgs()), req.getLocalCacheLifeTime());
                            if (result.result) {
                                if (!rpc.isTest()) {
                                    rpc.getMemoryCache().put(req.getMethod(), req.getArgs(), result.object, req.getLocalCacheSize());
                                }
                                cacheObjects.put(i, new Pair<JsonRequest, Object>(req, result.object));
                                if (req.isLocalCacheOnlyOnError()) {
                                    batches.remove(i);
                                }
                            }

                        }
                    }
                }
            }

            JsonBatchProgressObserver batchProgressObserver = new JsonBatchProgressObserver(rpc, batch, batches);
            List<JsonResult> responses;
            if (batches.size() > 0) {
                try {
                    responses = sendBatchRequest(batches, batchProgressObserver, cacheObjects.size());
                } catch (final Exception e) {
                    responses = new ArrayList<JsonResult>(batches.size());
                    for (JsonRequest request : batches) {
                        responses.add(new JsonErrorResult(request.getId(), e));
                    }
                }
                Collections.sort(responses);
            } else {
                responses = new ArrayList<JsonResult>();
                batchProgressObserver.setMaxProgress(1);
                batchProgressObserver.progressTick(1);
            }

            if (rpc.isCacheEnabled()) {
                for (int i = responses.size() - 1; i >= 0; i--) {
                    JsonResult result = responses.get(i);
                    if (cacheObjects.containsKey(result.id) && result instanceof JsonErrorResult) {
                        responses.remove(result);
                        responses.add(new JsonSuccessResult(cacheObjects.get(result.id)));
                        cacheObjects.remove(result.id);
                    }
                }

                for (Map.Entry<Integer, Pair<JsonRequest, Object>> pairs : cacheObjects.entrySet()) {
                    responses.add(pairs.getKey(), new JsonSuccessResult(pairs.getValue().second));
                    batches.add(pairs.getKey(), pairs.getValue().first);
                }
            }
            Collections.sort(batches, new Comparator<JsonRequest>() {
                @Override
                public int compare(JsonRequest lhs, JsonRequest rhs) {
                    return lhs.getId().compareTo(rhs.getId());
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

    private List<List<JsonRequest>> assignRequestsToConnections(List<JsonRequest> list, final int partsNo) {
        if (rpc.isTimeProfiler()) {
            return JsonBatchTask.timeAssignRequests(list, partsNo);
        } else {
            return JsonBatchTask.simpleAssignRequests(list, partsNo);
        }
    }


    private int calculateTimeout(List<JsonRequest> batches) {
        int timeout = 0;
        if (rpc.getTimeoutMode() == JsonBatchTimeoutMode.TIMEOUTS_SUM) {
            for (JsonRequest req : batches) {
                timeout += req.getTimeout();
            }
        } else {
            for (JsonRequest req : batches) {
                timeout = Math.max(timeout, req.getTimeout());
            }
        }
        return timeout;
    }

    private List<JsonResult> sendBatchRequest(List<JsonRequest> batches, JsonBatchProgressObserver progressObserver, int cachedRequests) throws Exception {
        int conn = rpc.getMaxConnections();

        if (batches.size() > 1 && conn > 1) {
            int connections = Math.min(batches.size(), conn);
            List<List<JsonRequest>> requestParts = assignRequestsToConnections(batches, connections);
            List<JsonResult> response = new ArrayList<JsonResult>(batches.size());
            List<JsonBatchTask> tasks = new ArrayList<JsonBatchTask>(connections);

            progressObserver.setMaxProgress((requestParts.size() + cachedRequests) * JsonTimeStat.TICKS);
            if (cachedRequests > 0) {
                progressObserver.progressTick(cachedRequests * JsonTimeStat.TICKS);
            }

            for (List<JsonRequest> requests : requestParts) {

                JsonBatchTask task = new JsonBatchTask(rpc, progressObserver, calculateTimeout(requests), requests);
                tasks.add(task);
            }
            for (JsonBatchTask task : tasks) {
                task.execute();
            }
            for (JsonBatchTask task : tasks) {
                task.join();
                if (task.getEx() != null) {
                    throw task.getEx();
                } else {
                    response.addAll(task.getResponse());
                }
            }
            return response;
        } else {
            progressObserver.setMaxProgress(JsonTimeStat.TICKS);
            return rpc.getJsonConnector().callBatch(batches, progressObserver, calculateTimeout(batches));
        }
    }

    private void handleBatchResponse(List<JsonRequest> requests, JsonBatch batch, List<JsonResult> responses) {
        Object[] results = new Object[requests.size()];
        Exception ex = null;
        int i = 0;
        for (JsonRequest request : requests) {
            try {
                JsonResult response = responses.get(i);

                if (response.cacheObject != null) {
                    results[i] = response.cacheObject;
                } else {


                    if (response.error != null) {
                        throw response.error;
                    }

                    if (request.getReturnType() != Void.class) {
                        if (rpc.isVerifyResultModel()) {
                            JsonConnector.verifyResult(request, response);
                        }
                        if (rpc.isProcessingMethod()) {
                            JsonConnector.processingMethod(response.result);
                        }
                        results[i] = response.result;
                        if ((rpc.isCacheEnabled() && request.isLocalCachable()) || rpc.isTest()) {
                            rpc.getMemoryCache().put(request.getMethod(), request.getArgs(), results[i], request.getLocalCacheSize());
                            if (rpc.getCacheMode() == JsonCacheMode.CLONE) {
                                results[i] = rpc.getJsonClonner().clone(results[i]);
                            }
                            JsonLocalCacheLevel cacheLevel = rpc.isTest() ? JsonLocalCacheLevel.DISK_CACHE : request.getLocalCacheLevel();

                            if (cacheLevel != JsonLocalCacheLevel.MEMORY_ONLY) {
                                JsonCacheMethod cacheMethod = new JsonCacheMethod(rpc.getTestName(), rpc.getTestRevision(), rpc.getUrl(), request.getMethod(), cacheLevel);
                                rpc.getDiskCache().put(cacheMethod, Arrays.deepToString(request.getArgs()), results[i], request.getLocalCacheSize());
                            }
                        } else if (rpc.isCacheEnabled() && request.isServerCachable() && (response.hash != null || response.time != null)) {
                            JsonCacheMethod cacheMethod = new JsonCacheMethod(rpc.getUrl(), request.getMethod(), response.hash, response.time, request.getServerCacheLevel());
                            rpc.getDiskCache().put(cacheMethod, Arrays.deepToString(request.getArgs()), results[i], request.getServerCacheSize());
                        }
                    }
                }
                request.invokeCallback(results[i]);
            } catch (Exception e) {
                if (request.isBatchFatal()) {
                    ex = e;
                }
                request.invokeCallback(new JsonException(request.getName(), e));
            }
            i++;
        }
        if (batch != null) {
            if (ex == null) {
                JsonRequest.invokeBatchCallback(rpc, batch, results);
            } else {
                JsonRequest.invokeBatchCallback(rpc, batch, ex);
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
