package com.implix.jsonrpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.*;

class JsonConnector {

    String url;
    JsonRpcImplementation rpc;
    JsonRpcVersion version = JsonRpcVersion.VERSION_2_0;
    JsonConnection connection;


    public JsonConnector(String url, JsonRpcImplementation rpc, JsonConnection connection) {
        this.url = url;
        this.rpc = rpc;
        this.connection = connection;
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    public void setJsonVersion(JsonRpcVersion version) {
        this.version = version;
    }


    private JsonResponseModel readResponse(InputStream stream) throws Exception {
        if ((rpc.getDebugFlags() & JsonRpc.RESPONSE_DEBUG) > 0) {

            String resStr = convertStreamToString(stream);
            JsonLoggerImpl.longLog("RES(" + resStr.length() + ")", resStr);
            if (version == JsonRpcVersion.VERSION_2_0) {
                JsonResponseModel2 response = rpc.getParser().fromJson(resStr, JsonResponseModel2.class);
                if (response.error != null) {
                    throw new JsonException(response.error.message, response.error.code);
                }
                return response;
            } else {
                JsonResponseModel1 response = rpc.getParser().fromJson(resStr, JsonResponseModel1.class);
                if (response.error != null) {
                    throw new JsonException(response.error);
                }
                return response;
            }
        } else {
            JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            JsonResponseModel response;

            if (version == JsonRpcVersion.VERSION_2_0) {
                JsonResponseModel2 response2 = rpc.getParser().fromJson(reader, JsonResponseModel2.class);
                if (response2.error != null) {
                    throw new JsonException(response2.error.message, response2.error.code);
                }
                response = response2;
            } else {
                JsonResponseModel1 response1 = rpc.getParser().fromJson(reader, JsonResponseModel1.class);
                if (response1.error != null) {
                    throw new JsonException(response1.error);
                }
                response = response1;
            }

            reader.close();
            return response;
        }

    }

    @SuppressWarnings("unchecked")
    public <T> T call(JsonRequest request) throws Exception {
        JsonTimeStat timeStat = new JsonTimeStat();

        if (rpc.isCacheEnabled() && request.isCachable()) {
            Object cacheObject = rpc.getCache().get(request.getName(), request.getArgs(), request.getCacheLifeTime());
            if (cacheObject != null) {
                return (T) cacheObject;
            }
        }
        HttpURLConnection conn = null;
        if (request.getMethodType() == JsonMethodType.JSON_RPC) {
            JsonRequestModel jsonRequest = request.createJsonRequest(version);
            timeStat.tickCreateTime();
            conn = connection.post(url, jsonRequest, request.getTimeout(), timeStat);
        } else {
            String getRequest = request.createGetRequest();
            timeStat.tickCreateTime();
            conn = connection.get(url, getRequest, request.getTimeout(), timeStat);
        }
        timeStat.tickConnectionTime();

        JsonResponseModel response = readResponse(conn.getInputStream());
        if (response == null) {
            throw new JsonException("Empty response.");
        }
        timeStat.tickReadTime();

        T res;
        if (!request.getReturnType().equals(Void.TYPE)) {
            res = parseResponse(response.result, request.getReturnType());
        } else {
            res = null;
        }

        conn.disconnect();

        timeStat.tickParseTime();

        if (rpc.isTimeProfiler()) {
            refreshStat(request.getName(), timeStat.getMethodTime());
        }

        timeStat.tickEndTime();
        if ((rpc.getDebugFlags() & JsonRpc.TIME_DEBUG) > 0) {
            timeStat.logTime("End single request(" + request.getName() + "):");
        }

        if (rpc.isCacheEnabled() && request.isCachable()) {
            rpc.getCache().put(request.getName(), request.getArgs(), res, request.getCacheSize());
        }

        return res;
    }

    private <T> T parseResponse(JsonElement result, Type type) {
        return rpc.getParser().fromJson(result, type);
    }

    public void notify(JsonRequest request) throws Exception {
        HttpURLConnection conn = connection.post(url, request, request.getTimeout(), new JsonTimeStat());
        conn.getInputStream().close();
    }


    public List<JsonResponseModel2> callBatch(List<JsonRequest> requests, Integer timeout) throws Exception {
        int i = 0;
        JsonTimeStat timeStat = new JsonTimeStat();
        JsonReader reader = null;
        Object[] requestsJson = new Object[requests.size()];
        List<JsonResponseModel2> responses = null;
        String requestsName = "";



        for (JsonRequest request : requests) {
            requestsJson[i] = request.createJsonRequest(version);
            requestsName += " " + request.getName();
            i++;
        }
        timeStat.tickCreateTime();
        HttpURLConnection conn = connection.post(url, requestsJson, timeout, timeStat);
        InputStream stream = conn.getInputStream();
        try {
            if ((rpc.getDebugFlags() & JsonRpc.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(stream);
                timeStat.tickReadTime();
                JsonLoggerImpl.longLog("RES(" + resStr.length() + ")", resStr);
                responses = rpc.getParser().fromJson(resStr,
                        new TypeToken<List<JsonResponseModel2>>() {
                        }.getType());
                timeStat.tickParseTime();
            } else {
                reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
                responses = rpc.getParser().fromJson(reader,
                        new TypeToken<List<JsonResponseModel2>>() {
                        }.getType());
                reader.close();
                timeStat.tickReadTime();
                timeStat.tickParseTime();
            }
        } catch (JsonSyntaxException e) {
            throw new JsonException(requestsName.substring(1), e);
        }
        if (responses == null) {
            throw new JsonException("Empty response.");
        }
        conn.disconnect();

        if (rpc.isTimeProfiler()) {

            for (JsonRequest request : requests) {
                refreshStat(request.getName(), timeStat.getMethodTime() / requests.size());
            }
        }


        timeStat.tickEndTime();
        if ((rpc.getDebugFlags() & JsonRpc.TIME_DEBUG) > 0) {
            timeStat.logTime("End batch request(" + requestsName.substring(1) + "):");
        }


        return responses;
    }

    private JsonStat getStat(String method) {
        JsonStat stat = null;
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
