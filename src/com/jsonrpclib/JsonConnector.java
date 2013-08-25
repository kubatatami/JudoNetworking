package com.jsonrpclib;


import android.util.Base64;
import android.util.Log;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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


    private JsonResult sendRequest(JsonRequest request, JsonTimeStat timeStat) {
        try {
            ProtocolController controller = rpc.getProtocolController();
            ProtocolController.RequestInfo requestInfo = controller.createRequest(url, request, rpc.getApiKey());
            timeStat.tickCreateTime();
            lossCheck();

            JsonConnection.Connection conn = connection.send(controller, requestInfo, request.getTimeout(), timeStat, rpc.getDebugFlags(), request.getMethod());

            JsonResult result = controller.parseResponse(request, conn.getStream(), rpc.getDebugFlags(), timeStat);
            verifyResultObject(result.result, request.getReturnType());
            conn.close();
            return result;
        } catch (Exception e) {
            return new JsonErrorResult(e);
        }

    }

    public static void verifyResultObject(Object object, Type type) throws JsonException {
        if (object != null) {
            for (Field field : object.getClass().getFields()) {
                try {
                    field.setAccessible(true);
                    if (field.get(object) == null) {
                        if (field.isAnnotationPresent(JsonRequired.class)) {
                            throw new JsonException("Field " + object.getClass().getName() + "." + field.getName() + " required.");
                        }
                    } else if (field.getType().isAnnotationPresent(JsonRequired.class)){
                        verifyResultObject(field.get(object), field.getType());
                    }
                } catch (IllegalAccessException e) {
                }
            }
        } else if (type instanceof Class && ((Class) type).isAnnotationPresent(JsonRequired.class)) {
            throw new JsonException("Result object required.");
        }
    }

    Object[] addElement(Object[] org, Object added) {
        Object[] result = Arrays.copyOf(org, org.length + 1);
        result[org.length] = added;
        return result;
    }

    class VirtualJsonCallback implements JsonCallbackInterface {
        private int id;
        private JsonResult result;

        VirtualJsonCallback(int id) {
            this.id = id;
        }

        @Override
        public void onFinish(Object result) {
            this.result = new JsonSuccessResult(id, result);
        }

        @Override
        public void onError(Exception e) {
            this.result = new JsonErrorResult(id, e);
        }

        @Override
        public void onProgress(int progress) {
            throw new IllegalAccessError("Virtual server can't invoke onProgress");
        }

        public JsonResult getResult() {
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    public Object call(JsonRequest request) throws Exception {
        JsonTimeStat timeStat = new JsonTimeStat(request);
        JsonVirtualServerInfo virtualServerInfo = rpc.getVirtualServers().get(request.getMethod().getDeclaringClass());

        if (virtualServerInfo != null) {

            int delay = randDelay(virtualServerInfo.minDelay, virtualServerInfo.maxDelay);

            for (int i = 0; i <= JsonTimeStat.TICKS; i++) {
                Thread.sleep(delay);
                timeStat.tickTime(i);
            }

            if (request.getCallback() == null) {
                return request.getMethod().invoke(virtualServerInfo.server, request.getArgs());
            } else {
                VirtualJsonCallback callback = new VirtualJsonCallback(request.getId());
                Object[] args = request.getArgs()!=null ? addElement(request.getArgs(), callback) : new Object[]{callback};
                request.getMethod().invoke(virtualServerInfo.server, args);
                if (callback.getResult().error != null) {
                    throw callback.getResult().error;
                }
                return callback.getResult().result;
            }


        } else {

            try {


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
    }

    public List<JsonResult> callBatch(List<JsonRequest> requests, JsonProgressObserver progressObserver, Integer timeout) throws Exception {

        JsonVirtualServerInfo virtualServerInfo = rpc.getVirtualServers().get(requests.get(0).getMethod().getDeclaringClass());

        if (virtualServerInfo != null) {
            List<JsonResult> results = new ArrayList<JsonResult>(requests.size());
            JsonTimeStat timeStat = new JsonTimeStat(progressObserver);


            int delay = randDelay(virtualServerInfo.minDelay, virtualServerInfo.maxDelay);
            for (int i = 0; i < JsonTimeStat.TICKS; i++) {
                Thread.sleep(delay/JsonTimeStat.TICKS);
                for(JsonRequest request : requests)
                {
                    timeStat.tickTime(i);
                }
            }

            for (JsonRequest request : requests) {

                VirtualJsonCallback callback = new VirtualJsonCallback(request.getId());
                Object[] args = request.getArgs()!=null ? addElement(request.getArgs(), callback) : new Object[]{callback};
                request.getMethod().invoke(virtualServerInfo.server, args);
                results.add(callback.getResult());
            }
            return results;
        } else {
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
                return callRealBatch(requests, progressObserver, timeout, requestsName);
            } else {
                List<JsonResult> results = new ArrayList<JsonResult>(requests.size());

                synchronized (progressObserver) {
                    progressObserver.setMaxProgress(progressObserver.getMaxProgress() + (requests.size() - 1) * JsonTimeStat.TICKS);
                }
                for (JsonRequest request : requests) {
                    JsonTimeStat timeStat = new JsonTimeStat(progressObserver);
                    results.add(sendRequest(request, timeStat));
                }

                return results;
            }
        }
    }

    public List<JsonResult> callRealBatch(List<JsonRequest> requests, JsonProgressObserver progressObserver, Integer timeout, String requestsName) throws Exception {

        try {
            ProtocolController controller = rpc.getProtocolController();
            List<JsonResult> responses = null;
            JsonTimeStat timeStat = new JsonTimeStat(progressObserver);


            ProtocolController.RequestInfo requestInfo = controller.createRequest(url, (List) requests, rpc.getApiKey());
            timeStat.tickCreateTime();
            lossCheck();
            JsonConnection.Connection conn = connection.send(controller, requestInfo, timeout, timeStat, rpc.getDebugFlags(), null);
            InputStream stream = conn.getStream();
            responses = controller.parseResponses((List) requests, stream, rpc.getDebugFlags(), timeStat);
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