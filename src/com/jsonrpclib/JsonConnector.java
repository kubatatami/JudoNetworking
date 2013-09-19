package com.jsonrpclib;


import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

class JsonConnector {

    private final String url;
    private final JsonRpcImplementation rpc;
    private final JsonConnection connection;
    private final Random randomGenerator = new Random();

    public JsonConnector(String url, JsonRpcImplementation rpc, JsonConnection connection) {
        this.url = url;
        this.rpc = rpc;
        this.connection = connection;
    }

    private static void longLog(String tag, String message) {
        JsonLoggerImpl.longLog(tag, message);
    }

    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private JsonResult sendRequest(JsonRequest request, JsonTimeStat timeStat) {
        try {

            Object virtualObject = handleVirtualServerRequest(request, timeStat);
            if (virtualObject != null) {
                return new JsonSuccessResult(request.getId(), virtualObject);
            }

            ProtocolController controller = rpc.getProtocolController();
            ProtocolController.RequestInfo requestInfo = controller.createRequest(url, request);
            timeStat.tickCreateTime();
            lossCheck();

            JsonConnection.Connection conn = connection.send(controller, requestInfo, request.getTimeout(), timeStat, rpc.getDebugFlags(), request.getMethod());
            InputStream connectionStream = conn.getStream();
            if ((rpc.getDebugFlags() & JsonRpc.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(conn.getStream());
                longLog("RES(" + resStr.length() + ")", resStr);
                connectionStream = new ByteArrayInputStream(resStr.getBytes("UTF-8"));
            }
            JsonInputStream stream = new JsonInputStream(connectionStream, timeStat, conn.getContentLength());
            JsonResult result = controller.parseResponse(request, stream);
            timeStat.tickParseTime();
            if(rpc.isVerifyResultModel())
            {
                verifyResultObject(result.result, request.getReturnType());
            }
            conn.close();
            return result;
        } catch (Exception e) {
            return new JsonErrorResult(request.getId(),e);
        }

    }

    public static void verifyResultObject(Object object, Type type) throws JsonException {
        if (object != null) {
            if (object instanceof Iterable) {
                for (Object obj : ((Iterable) object)) {
                    verifyResultObject(obj, obj.getClass());
                }
            } else {
                for (Field field : object.getClass().getFields()) {
                    try {
                        field.setAccessible(true);

                        if (field.get(object) == null) {
                            if (field.isAnnotationPresent(JsonRequired.class)) {
                                throw new JsonException("Field " + object.getClass().getName() + "." + field.getName() + " required.");
                            }
                        } else {
                            JsonRequired ann = field.getAnnotation(JsonRequired.class);
                            Object iterableObject = field.get(object);
                            if (iterableObject instanceof Iterable) {
                                int i=0;
                                for (Object obj : (Iterable) iterableObject) {
                                    verifyResultObject(obj, obj.getClass());
                                    i++;
                                }
                                if(ann!=null && !ann.allowEmpty() && i==0)
                                {
                                    throw new JsonException("List " + object.getClass().getName() + "." + field.getName() + " is empty.");
                                }
                            } else if (ann!=null) {
                                verifyResultObject(field.get(object), field.getType());
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (type instanceof Class && ((Class) type).isAnnotationPresent(JsonRequired.class)) {
            throw new JsonException("Result object required.");
        }
    }

    public static Object[] addElement(Object[] org, Object added) {
        Object[] result = new Object[org.length + 1];
        System.arraycopy(org, 0, result, 0, org.length);
        result[org.length] = added;
        return result;
    }


    private Object handleVirtualServerRequest(JsonRequest request, JsonTimeStat timeStat) throws Exception {
        JsonVirtualServerInfo virtualServerInfo = rpc.getVirtualServers().get(request.getMethod().getDeclaringClass());
        if (virtualServerInfo != null) {
            if (request.getCallback() == null) {
                try {
                    Object object = request.getMethod().invoke(virtualServerInfo.server, request.getArgs());
                    int delay = randDelay(virtualServerInfo.minDelay, virtualServerInfo.maxDelay);
                    for (int i = 0; i <= JsonTimeStat.TICKS; i++) {
                        Thread.sleep(delay / JsonTimeStat.TICKS);
                        timeStat.tickTime(i);
                    }
                    return object;
                } catch (InvocationTargetException ex) {
                    if (ex.getCause() == null || !(ex.getCause() instanceof UnsupportedOperationException)) {
                        throw ex;
                    }
                }

            } else {
                JsonVirtualCallback callback = new JsonVirtualCallback(request.getId());
                Object[] args = request.getArgs() != null ? addElement(request.getArgs(), callback) : new Object[]{callback};
                boolean implemented = true;
                try {
                    request.getMethod().invoke(virtualServerInfo.server, args);
                    int delay = randDelay(virtualServerInfo.minDelay, virtualServerInfo.maxDelay);
                    for (int i = 0; i <= JsonTimeStat.TICKS; i++) {
                        Thread.sleep(delay / JsonTimeStat.TICKS);
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
        return null;
    }


    @SuppressWarnings("unchecked")
    public Object call(JsonRequest request) throws Exception {
        try {

            JsonTimeStat timeStat = new JsonTimeStat(request);


            if ((rpc.isCacheEnabled() && request.isCachable()) || rpc.isTest()) {
                JsonCacheResult cacheObject = rpc.getMemoryCache().get(request.getMethod(), request.getArgs(), rpc.isTest() ? 0 : request.getCacheLifeTime(), request.getCacheSize());
                if (cacheObject.result) {
                    timeStat.tickCacheTime();
                    return cacheObject.object;
                } else if (request.isCachePersist() || rpc.isTest()) {
                    JsonCacheMethod cacheMethod = new JsonCacheMethod(rpc.getTestName(), rpc.getTestRevision(), url, request.getMethod());
                    cacheObject = rpc.getDiscCache().get(cacheMethod, request.getArgs(), request.getCacheLifeTime(), request.getCacheSize());
                    if (cacheObject.result) {
                        if (!rpc.isTest()) {
                            rpc.getMemoryCache().put(request.getMethod(), request.getArgs(), cacheObject.object, request.getCacheSize());
                        }
                        timeStat.tickCacheTime();
                        return cacheObject.object;
                    }

                }
            }


            if (request.getArgs() != null && rpc.isByteArrayAsBase64()) {
                int i = 0;
                for (Object object : request.getArgs()) {
                    if (object instanceof byte[]) {
                        request.getArgs()[i] = Base64.encodeToString((byte[]) object, Base64.NO_WRAP);
                    }
                    i++;
                }
            }

            JsonResult result = sendRequest(request, timeStat);
            if (result.error != null) {
                throw result.error;
            }


            timeStat.tickEndTime();


            if (rpc.isTimeProfiler()) {
                refreshStat(request.getName(), timeStat.getMethodTime());
            }

            if ((rpc.getDebugFlags() & JsonRpc.TIME_DEBUG) > 0) {
                timeStat.logTime("End single request(" + request.getName() + "):");
            }

            if ((rpc.isCacheEnabled() && request.isCachable()) || rpc.isTest()) {
                rpc.getMemoryCache().put(request.getMethod(), request.getArgs(), result.result, request.getCacheSize());
                if (rpc.getCacheMode() == JsonCacheMode.CLONE) {
                    result.result = rpc.getJsonClonner().clone(result.result);
                }

                if (request.isCachePersist() || rpc.isTest()) {
                    JsonCacheMethod cacheMethod = new JsonCacheMethod(rpc.getTestName(), rpc.getTestRevision(), url, request.getMethod());
                    rpc.getDiscCache().put(cacheMethod, request.getArgs(), result.result, request.getCacheSize());
                }


            }

            return result.result;
        } catch (Exception e) {
            refreshErrorStat(request.getName(), request.getTimeout());
            throw e;
        }

    }

    public List<JsonResult> callBatch(List<JsonRequest> requests, JsonProgressObserver progressObserver, Integer timeout) throws Exception {
        List<JsonResult> results = new ArrayList<JsonResult>(requests.size());


        if (requests.size() > 0) {
            String requestsName = "";
            for (JsonRequest request : requests) {
                requestsName += " " + request.getName();
                if (request.getArgs() != null && rpc.isByteArrayAsBase64()) {
                    int i = 0;
                    for (Object object : request.getArgs()) {
                        if (object instanceof byte[]) {
                            request.getArgs()[i] = Base64.encodeToString((byte[]) object, Base64.NO_WRAP);
                        }
                        i++;
                    }
                }
            }

            if (rpc.getProtocolController().isBatchSupported()) {

                List<JsonRequest> copyRequest = new ArrayList<JsonRequest>(requests);
                JsonVirtualServerInfo virtualServerInfo = rpc.getVirtualServers().get(requests.get(0).getMethod().getDeclaringClass());
                if (virtualServerInfo != null) {

                    JsonTimeStat timeStat = new JsonTimeStat(progressObserver);

                    int delay = randDelay(virtualServerInfo.minDelay, virtualServerInfo.maxDelay);

                    for (int i = copyRequest.size() - 1; i >= 0; i--) {
                        JsonRequest request = copyRequest.get(i);

                        JsonVirtualCallback callback = new JsonVirtualCallback(request.getId());
                        Object[] args = request.getArgs() != null ? addElement(request.getArgs(), callback) : new Object[]{callback};
                        boolean implemented = true;
                        try {
                            request.getMethod().invoke(virtualServerInfo.server, args);

                        } catch (InvocationTargetException ex) {
                            if (ex.getCause() != null && ex.getCause() instanceof UnsupportedOperationException) {
                                implemented = false;
                            } else {
                                throw ex;
                            }
                        }
                        if (implemented) {
                            results.add(callback.getResult());
                            copyRequest.remove(request);
                        }
                    }
                    if (copyRequest.size() == 0) {
                        for (int z = 0; z < JsonTimeStat.TICKS; z++) {
                            Thread.sleep(delay / JsonTimeStat.TICKS);
                            timeStat.tickTime(z);
                        }
                    }
                }
                if (copyRequest.size() > 0) {
                    results.addAll(callRealBatch(copyRequest, progressObserver, timeout, requestsName));
                }
            } else {
                synchronized (progressObserver) {
                    progressObserver.setMaxProgress(progressObserver.getMaxProgress() + (requests.size() - 1) * JsonTimeStat.TICKS);
                }
                for (JsonRequest request : requests) {
                    JsonTimeStat timeStat = new JsonTimeStat(progressObserver);
                    results.add(sendRequest(request, timeStat));
                }
            }
        }
        return results;
    }

    public List<JsonResult> callRealBatch(List<JsonRequest> requests, JsonProgressObserver progressObserver, Integer timeout, String requestsName) throws Exception {

        try {
            ProtocolController controller = rpc.getProtocolController();
            List<JsonResult> responses = null;
            JsonTimeStat timeStat = new JsonTimeStat(progressObserver);


            ProtocolController.RequestInfo requestInfo = controller.createRequest(url, (List) requests);
            timeStat.tickCreateTime();
            lossCheck();
            JsonConnection.Connection conn = connection.send(controller, requestInfo, timeout, timeStat, rpc.getDebugFlags(), null);
            InputStream connectionStream = conn.getStream();
            if ((rpc.getDebugFlags() & JsonRpc.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(conn.getStream());
                longLog("RES(" + resStr.length() + ")", resStr);
                connectionStream = new ByteArrayInputStream(resStr.getBytes("UTF-8"));
            }

            JsonInputStream stream = new JsonInputStream(connectionStream, timeStat, conn.getContentLength());
            responses = controller.parseResponses((List) requests, stream);
            timeStat.tickParseTime();
            conn.close();
            timeStat.tickEndTime();
            if (rpc.isTimeProfiler()) {

                for (JsonRequest request : requests) {
                    refreshStat(request.getName(), timeStat.getMethodTime() / requests.size());
                }
            }


            if ((rpc.getDebugFlags() & JsonRpc.TIME_DEBUG) > 0) {
                timeStat.logTime("End batch request(" + requestsName.substring(1) + "):");
            }

            return responses;
        } catch (Exception e) {
            for (JsonRequest request : requests) {
                refreshErrorStat(request.getName(), request.getTimeout());
            }
            throw new JsonException(requestsName.substring(1), e);
        }
    }


    private void lossCheck() throws JsonException {
        if (rpc.getPercentLoss() != 0 && randomGenerator.nextFloat() < rpc.getPercentLoss()) {
            throw new JsonException("Random package lost.");
        }
    }

    private JsonStat getStat(String method) {
        JsonStat stat;
        if (rpc.getStats().containsKey(method)) {
            stat = rpc.getStats().get(method);
        } else {
            stat = new JsonStat();
            rpc.getStats().put(method, stat);
        }
        return stat;
    }

    private void refreshStat(String method, long time) {
        JsonStat stat = getStat(method);
        stat.avgTime = ((stat.avgTime * stat.requestCount) + time) / (stat.requestCount + 1);
        stat.requestCount++;
        rpc.saveStat();
    }

    private void refreshErrorStat(String method, long timeout) {
        JsonStat stat = getStat(method);
        stat.avgTime = ((stat.avgTime * stat.requestCount) + timeout) / (stat.requestCount + 1);
        stat.errors++;
        stat.requestCount++;
        rpc.saveStat();
    }

    public void setReconnections(int reconnections) {
        connection.setReconnections(reconnections);
    }

    public void setConnectTimeout(int connectTimeout) {
        connection.setConnectTimeout(connectTimeout);
    }

    public void setMethodTimeout(int methodTimeout) {
        connection.setMethodTimeout(methodTimeout);
    }

    public int getMethodTimeout() {
        return connection.getMethodTimeout();
    }

    public int randDelay(int minDelay, int maxDelay) {
        if (maxDelay == 0) {
            return 0;
        }
        Random random = new Random();
        return minDelay + random.nextInt(maxDelay - minDelay);
    }
}
