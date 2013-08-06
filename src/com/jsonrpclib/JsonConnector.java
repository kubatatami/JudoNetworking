package com.jsonrpclib;


import android.util.Base64;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

class JsonConnector {

    private final String url;
    private final JsonRpcImplementation rpc;
    private final JsonConnection connection;


    public JsonConnector(String url, JsonRpcImplementation rpc, JsonConnection connection) {
        this.url = url;
        this.rpc = rpc;
        this.connection = connection;
    }


    private JsonResult sendRequest(JsonRequest request, JsonTimeStat timeStat) {
        try {
            ProtocolController controller = rpc.getProtocolController();
            HttpURLConnection conn;
            ProtocolController.RequestInfo requestInfo = controller.createRequest(url, request, rpc.getApiKey());
            timeStat.tickCreateTime();
            if (controller.getConnectionType() == ProtocolController.ConnectionType.GET) {
                conn = connection.get(requestInfo.url, request.getTimeout(), timeStat);
            } else {
                conn = connection.post(controller, requestInfo.url, requestInfo.postRequest, request.getTimeout(), timeStat);
            }

            timeStat.tickConnectionTime();

            JsonResult result = controller.parseResponse(request, conn.getInputStream(), rpc.getDebugFlags(), timeStat);
            conn.disconnect();
            return result;
        } catch (Exception e) {
            return new JsonResult(e);
        }

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

            JsonResult result = sendRequest(request,timeStat);
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
            JsonTimeStat timeStat = new JsonTimeStat(progressObserver);
            synchronized (progressObserver)
            {
                progressObserver.setMaxProgress(progressObserver.getMaxProgress()+(requests.size()-1)*JsonTimeStat.TICKS);
            }
            for (JsonRequest request : requests) {
                results.add(sendRequest(request, timeStat));
            }

            return results;
        }

    }

    public List<JsonResult> callRealBatch(List<JsonRequest> requests, JsonProgressObserver progressObserver, Integer timeout, String requestsName) throws Exception {

        try {
            ProtocolController controller = rpc.getProtocolController();
            List<JsonResult> responses = null;
            JsonTimeStat timeStat = new JsonTimeStat(progressObserver);


            ProtocolController.RequestInfo requestInfo = controller.createRequest(url, requests, rpc.getApiKey());
            timeStat.tickCreateTime();
            HttpURLConnection conn = null;
            if (controller.getConnectionType() == ProtocolController.ConnectionType.GET) {
                conn = connection.get(requestInfo.url, timeout, timeStat);
            } else {
                conn = connection.post(controller, requestInfo.url, requestInfo.postRequest, timeout, timeStat);
            }
            InputStream stream = conn.getInputStream();
            responses = controller.parseResponses(requests, stream, rpc.getDebugFlags(), timeStat);
            conn.disconnect();
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


}
