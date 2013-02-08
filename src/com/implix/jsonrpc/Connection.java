package com.implix.jsonrpc;

import android.os.Build;
import android.text.TextUtils;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

class Connection {

    String url;
    JsonRpcImplementation rpc;
    JsonRpcVersion version = JsonRpcVersion.VERSION_2_0;
    int flags = 0;

    public Connection(String url, JsonRpcImplementation rpc) {
        this.url = url;
        this.rpc = rpc;
        disableConnectionReuseIfNecessary();
    }

    private void disableConnectionReuseIfNecessary() {
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }


    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void longStrToConsole(String tag, String str) {
        System.out.println(tag+":");
        int i;
        for (i = 0; i < str.length() - 256; i += 256) {
            System.out.println(str.substring(i, i + 256));
        }
        System.out.println(str.substring(i, str.length()));

    }


    private JsonRequestModel createRequest(Integer currId, String name, String[] params, Object[] args, String apiKey) {
        Object finalArgs = null;
        if (params != null) {
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

    private HttpURLConnection conn(Object request, Integer timeout) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.addRequestProperty("Content-Type", "application/json");

        if (rpc.getAuthKey() != null) {
            urlConnection.addRequestProperty("Authorization", rpc.getAuthKey());
        }

        urlConnection.setConnectTimeout(10000);
        if (timeout != null) {
            urlConnection.setReadTimeout(timeout);
        } else {
            urlConnection.setReadTimeout(10000);
        }
        urlConnection.setDoOutput(true);
        Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));

        if ((flags & JsonRpc.REQUEST_DEBUG) > 0) {
            String req = rpc.getParser().toJson(request);
            longStrToConsole("REQ",req);
            writer.write(req);
        } else {
            rpc.getParser().toJson(request, writer);
        }

        writer.close();

        return urlConnection;

    }


    private JsonResponseModel readResponse(InputStream stream) throws Exception {
        if ((flags & JsonRpc.RESPONSE_DEBUG) > 0) {

            String resStr = convertStreamToString(stream);
            longStrToConsole("RES",resStr);

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

    public <T> T call(int id, String name, String[] params, Object[] args, Type type, Integer timeout, String apiKey) throws Exception {
        HttpURLConnection conn = conn(createRequest(id, name, params, args, apiKey), timeout);

        JsonResponseModel response = readResponse(conn.getInputStream());

        T res;
        if (!type.equals(Void.TYPE)) {
            res = parseResponse(response.result, type);
        } else {
            res = null;
        }

        conn.disconnect();
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


    public void callBatch(List<JsonRequest> requests, JsonTransactionCallback transactionCallback) {
        int i = 0;
        long createTime = 0, connectionTime = 0, readTime = 0, parseTime = 0, time = System.currentTimeMillis();
        long startTime = time;
        String connectionType = "";
        HttpURLConnection conn = null;
        JsonReader reader = null;
        Object[] results = new Object[requests.size()];
        Object[] requestsJson = new Object[requests.size()];
        Exception ex = null;
        for (JsonRequest request : requests) {
            requestsJson[i] = createRequest(request.getId(), request.getName(), request.getParams(),
                    request.getArgs(), request.getApiKey());
            i++;
        }

        createTime = System.currentTimeMillis() - time;
        time = System.currentTimeMillis();

        try {
            conn = conn(requestsJson, rpc.getTimeout());
            InputStream stream = conn.getInputStream();


            if (conn.getHeaderField("Connection") != null) {
                connectionType += conn.getHeaderField("Connection");
            }

            if (conn.getHeaderField("Content-Encoding") != null) {
                connectionType += "," + conn.getHeaderField("Content-Encoding");
            }

            connectionTime = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();

            List<JsonResponseModel2> responses = null;

            if ((flags & JsonRpc.RESPONSE_DEBUG) > 0) {

                String resStr = convertStreamToString(stream);
                longStrToConsole("RES",resStr);
                responses = rpc.getParser().fromJson(resStr,
                        new TypeToken<List<JsonResponseModel2>>() {
                        }.getType());
            } else {
                reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
                responses = rpc.getParser().fromJson(reader,
                        new TypeToken<List<JsonResponseModel2>>() {
                        }.getType());
            }


            readTime = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();

            i = 0;
            for (JsonRequest request : requests) {
                try {
                    JsonResponseModel2 response = responses.get(i);
                    if (response.error != null) {
                        throw new JsonException(request.getName() + ": " + response.error.message, response.error.code);
                    }
                    results[i] = parseResponse(response.result, request.getType());
                    request.invokeCallback(results[i]);
                } catch (Exception e) {
                    if (ex == null) {
                        ex = e;
                    }
                    request.invokeCallback(e);
                }
                i++;
            }

        } catch (IOException e) {
            for (JsonRequest request : requests) {
                request.invokeCallback(e);
            }
            ex = e;
        } finally {

            requests.clear();
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
            if (conn != null) {
                conn.disconnect();
            }

            if (transactionCallback != null) {
                if (ex == null) {
                    JsonRequest.invokeTransactionCallback(rpc, transactionCallback, results);
                } else {
                    JsonRequest.invokeTransactionCallback(rpc, transactionCallback, ex);
                }
            }

            parseTime = System.currentTimeMillis() - time;
            if ((flags & JsonRpc.TIME_DEBUG) > 0) {
                System.out.println("Transaction(" + connectionType + "): createRequests(" + (createTime) + "ms)" +
                        " connection&send(" + connectionTime + "ms)" +
                        " read(" + readTime + "ms) parse(" + parseTime + "ms)" +
                        " all(" + (System.currentTimeMillis() - startTime) + "ms)");
            }

        }

    }


    public void setDebugFlags(int flags) {
        this.flags = flags;
    }

}
