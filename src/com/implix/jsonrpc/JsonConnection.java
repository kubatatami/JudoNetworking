package com.implix.jsonrpc;

import android.os.Build;
import android.util.Base64;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;

class JsonConnection {

    String url;
    JsonRpcImplementation rpc;
    JsonRpcVersion version = JsonRpcVersion.VERSION_2_0;
    int reconnections = 3;
    int connectTimeout = 15000;
    int methodTimeout = 10000;


    public JsonConnection(String url, JsonRpcImplementation rpc) {
        this.url = url;
        this.rpc = rpc;
        disableConnectionReuseIfNecessary();
    }

    private void disableConnectionReuseIfNecessary() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }


    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void longStrToConsole(String tag, String str) {
        JsonLoggerImpl.log(tag + ":");
        int i;
        for (i = 0; i < str.length() - 256; i += 256) {
            JsonLoggerImpl.log(str.substring(i, i + 256));
        }
        JsonLoggerImpl.log(str.substring(i, str.length()));

    }


    private JsonRequestModel createRequest(Integer currId, String name, String[] params, Object[] args, String apiKey) {
        Object finalArgs = null;
        if (args != null && rpc.isByteArrayAsBase64()) {
            int i = 0;
            for (Object object : args) {
                if (object instanceof byte[]) {
                    args[i] = Base64.encodeToString((byte[]) object, Base64.NO_WRAP);
                }
                i++;
            }
        }

        if (params != null && args!=null) {
            int i = 0;
            Map<String, Object> paramObjects = new HashMap<String, Object>();
            for (String param : params) {
                paramObjects.put(param, args[i]);
                i++;
            }
            finalArgs = paramObjects;
            if (apiKey != null) {
                finalArgs = new Object[]{apiKey, finalArgs};
            }
        } else {
            finalArgs = args;
            if (apiKey != null) {
                if (args != null) {
                    Object[] finalArray = new Object[args.length + 1];
                    finalArray[0] = apiKey;
                    System.arraycopy(args, 0, finalArray, 1, args.length);
                    finalArgs = finalArray;
                } else {
                    finalArgs = new Object[]{apiKey};
                }
            }
        }
        if (version == JsonRpcVersion.VERSION_1_0_NO_ID) {
            return new JsonRequestModel(name, finalArgs, null);
        } else if (version == JsonRpcVersion.VERSION_1_0) {
            return new JsonRequestModel(name, finalArgs, currId);
        } else {
            return new JsonRequestModel2(name, finalArgs, currId);
        }


    }

    public void setJsonVersion(JsonRpcVersion version) {
        this.version = version;
    }

    private HttpURLConnection conn(Object request, int timeout) throws IOException {
        HttpURLConnection urlConnection = null;
//        if (flags > 0) {
//            System.out.println("Connection: start");
//        }
        for (int i = 1; i <= reconnections; i++) {
            try {
                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                break;
            } catch (IOException e) {
                if (i == reconnections) {
                    throw e;
                }
            }
        }
        urlConnection.addRequestProperty("Content-Type", "application/json");

        if (rpc.getAuthKey() != null) {
            urlConnection.addRequestProperty("Authorization", rpc.getAuthKey());
        }

        urlConnection.setConnectTimeout(connectTimeout);
        if (timeout == 0) {
            timeout = methodTimeout;
        }

        urlConnection.setReadTimeout(timeout);

        urlConnection.setDoOutput(true);
        Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));

        if ((rpc.getDebugFlags() & JsonRpc.REQUEST_DEBUG) > 0) {
            String req = rpc.getParser().toJson(request);
            longStrToConsole("REQ", req);
            writer.write(req);
        } else {
            rpc.getParser().toJson(request, writer);
        }

        writer.close();

        return urlConnection;

    }


    private JsonResponseModel readResponse(InputStream stream) throws Exception {
        if ((rpc.getDebugFlags() & JsonRpc.RESPONSE_DEBUG) > 0) {

            String resStr = convertStreamToString(stream);
            longStrToConsole("RES(" + resStr.length() + ")", resStr);

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

    public <T> T call(int id, String name, String[] params, Object[] args, Type type, int timeout, String apiKey, boolean cachable, int cacheLifeTime,int cacheSize) throws Exception {
        long createTime = 0, connectionTime = 0, readTime = 0, parseTime = 0, time = System.currentTimeMillis();
        long startTime = time;
        String connectionType = "";

        if(rpc.isCacheEnabled() && cachable)
        {
            Object cacheObject=rpc.getCache().get(name,args,cacheLifeTime);
            if(cacheObject!=null)
            {
                return (T) cacheObject;
            }
        }

        JsonRequestModel request = createRequest(id, name, params, args, apiKey);

        createTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();

        HttpURLConnection conn = conn(request, timeout);

        connectionTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();


        if (conn.getHeaderField("Connection") != null) {
            connectionType += conn.getHeaderField("Connection");
        }

        if (conn.getHeaderField("Content-Encoding") != null) {
            connectionType += "," + conn.getHeaderField("Content-Encoding");
        }

        JsonResponseModel response = readResponse(conn.getInputStream());

        if(response==null)
        {
            throw new JsonException("Empty response.");
        }

        readTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();

        T res;
        if (!type.equals(Void.TYPE)) {
            res = parseResponse(response.result, type);
        } else {
            res = null;
        }


        parseTime = System.currentTimeMillis() - time;

        if ((rpc.getDebugFlags() & JsonRpc.TIME_DEBUG) > 0) {
            JsonLoggerImpl.log("Single request(" + name + "): connection(" + connectionType + ")" +
                    " timeout(" + conn.getReadTimeout() + "ms) create(" + (createTime) + "ms)" +
                    " connection&send(" + connectionTime + "ms)" +
                    " read(" + readTime + "ms) parse(" + parseTime + "ms)" +
                    " all(" + (System.currentTimeMillis() - startTime) + "ms)");
        }

        conn.disconnect();

        if(rpc.isCacheEnabled())
        {
            rpc.getCache().put(name,args,res, cacheSize);
        }

        return res;

    }

    private <T> T parseResponse(JsonElement result, Type type) {
        return rpc.getParser().fromJson(result, type);
    }

    public void notify(String name, String[] params, Object[] args, Integer timeout, String apiKey) throws Exception {
        HttpURLConnection conn = conn(createRequest(null, name, params, args, apiKey), timeout);
        conn.getInputStream();
        conn.disconnect();
    }


    public List<JsonResponseModel2> callBatch(List<JsonRequest> requests, Integer timeout) throws Exception {
        int i = 0;
        long createTime, connectionTime = 0, time = System.currentTimeMillis();
        long startTime = time;
        String connectionType = "";
        HttpURLConnection conn = null;
        JsonReader reader = null;
        Object[] requestsJson = new Object[requests.size()];
        List<JsonResponseModel2> responses = null;
        String requestsName = "";


        for (JsonRequest request : requests) {
            requestsJson[i] = createRequest(request.getId(), request.getName(), request.getParams(),
                    request.getArgs(), request.getApiKey());
            requestsName += " " + request.getName();
            i++;
        }

        createTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();

        conn = conn(requestsJson, timeout);
        InputStream stream = conn.getInputStream();

        if (conn.getHeaderField("Connection") != null) {
            connectionType += conn.getHeaderField("Connection");
        }

        if (conn.getHeaderField("Content-Encoding") != null) {
            connectionType += "," + conn.getHeaderField("Content-Encoding");
        }

        connectionTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();

        if ((rpc.getDebugFlags() & JsonRpc.RESPONSE_DEBUG) > 0) {

            String resStr = convertStreamToString(stream);
            longStrToConsole("RES(" + resStr.length() + ")", resStr);
            responses = rpc.getParser().fromJson(resStr,
                    new TypeToken<List<JsonResponseModel2>>() {
                    }.getType());
        } else {
            reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
            responses = rpc.getParser().fromJson(reader,
                    new TypeToken<List<JsonResponseModel2>>() {
                    }.getType());
            reader.close();
        }

        if(responses==null)
        {
            throw new JsonException("Empty response.");
        }

        if ((rpc.getDebugFlags() & JsonRpc.TIME_DEBUG) > 0) {
            JsonLoggerImpl.log("Batch request(" + requestsName.substring(1) + "):" +
                    " connection(" + connectionType + ")" +
                    " timeout(" + conn.getReadTimeout() + "ms)" +
                    " createRequests(" + (createTime) + "ms)" +
                    " connection&send(" + connectionTime + "ms)" +
                    " read(" + (System.currentTimeMillis() - time) + "ms)" +
                    " all(" + (System.currentTimeMillis() - startTime) + "ms)");
        }

        conn.disconnect();
        return responses;
    }




    public void setReconnections(int reconnections) {
        this.reconnections = reconnections;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setMethodTimeout(int methodTimeout) {
        this.methodTimeout = methodTimeout;
    }

    public int getMethodTimeout() {
        return methodTimeout;
    }
}
