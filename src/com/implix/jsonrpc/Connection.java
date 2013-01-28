package com.implix.jsonrpc;

import android.content.Context;
import android.os.Build;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Connection {

    String url;
    JsonRpcImplementation rpc;

    public Connection(Context context, String url, JsonRpcImplementation rpc) {
        this.url = url;
        this.rpc = rpc;
        disableConnectionReuseIfNecessary();
        //enableHttpResponseCache(context);
    }

    private void disableConnectionReuseIfNecessary() {
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private void enableHttpResponseCache(Context context) {
        try {
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            File httpCacheDir = new File(context.getCacheDir(), "http");
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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
        } else {
            finalArgs = args;
        }
        if (apiKey != null) {
            finalArgs = new Object[]{apiKey, finalArgs};
        }
        return new JsonRequestModel(name, finalArgs, currId);
    }

    private HttpURLConnection conn(Object request, Integer timeout) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();

        urlConnection.setConnectTimeout(10000);
        if (timeout != null) {
            urlConnection.setReadTimeout(timeout);
        } else {
            urlConnection.setReadTimeout(10000);
        }
        urlConnection.setDoOutput(true);
        Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));

        String req=rpc.getParser().toJson(request);
        System.out.println("REQ:" + req);

        //rpc.getParser().toJson(request,writer);

        writer.write(req);
        writer.close();

        return urlConnection;

    }

    public <T> T call(int id, String name, String[] params, Object[] args, Type type, Integer timeout, String apiKey) throws Exception {
        HttpURLConnection conn = conn(createRequest(id, name, params, args, apiKey), timeout);

        JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        JsonResponseModel response = rpc.getParser().fromJson(reader, JsonResponseModel.class);

        if (response.error != null) {
            throw new JsonException(response.error.message,response.error.code);
        }
        T res;
        if(!type.equals(Void.TYPE))
        {
            res = parseResponse(response.result, type);
        }
        else
        {
            res=null;
        }

        reader.close();
        conn.disconnect();
        return res;

    }

    private <T> T parseResponse(JsonElement result,Type type)
    {
        return rpc.getParser().fromJson(result, type);
    }

    public void notify(String name, String[] params, Object[] args, Integer timeout, String apiKey) throws Exception {
        HttpURLConnection conn = conn(createRequest(null, name, params, args, apiKey), timeout);
        conn.getInputStream();
        conn.disconnect();
    }


    public void callBatch(List<JsonRequest> requests, JsonTransactionCallback transactionCallback) {
        int i = 0;
        long createTime=0, connectionTime=0,readTime=0,parseTime=0,time=System.currentTimeMillis();
        long startTime=time;
        String connectionType="";
        HttpURLConnection conn=null;
        JsonReader reader=null;
        Object[] results = new Object[requests.size()];
        JsonRequestModel[] requestsJson = new JsonRequestModel[requests.size()];
        Exception ex=null;
        for (JsonRequest request : requests) {
            requestsJson[i] = createRequest(request.getId(), request.getName(), request.getParams(),
                    request.getArgs(), request.getApiKey());
            i++;
        }

        createTime=System.currentTimeMillis()-time;
        time=System.currentTimeMillis();

        try {
            conn = conn(requestsJson, rpc.getTimeout());

            reader = new JsonReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            if(conn.getHeaderField("Connection")!=null)
            {
                connectionType+=conn.getHeaderField("Connection");
            }

            if(conn.getHeaderField("Content-Encoding")!=null)
            {
                connectionType+=","+conn.getHeaderField("Content-Encoding");
            }

            connectionTime=System.currentTimeMillis()-time;
            time=System.currentTimeMillis();

            List<JsonResponseModel> responses = rpc.getParser().fromJson(reader,
                    new TypeToken<List<JsonResponseModel>>() {}.getType());

            readTime=System.currentTimeMillis()-time;
            time=System.currentTimeMillis();

            i = 0;
            for (JsonRequest request : requests) {
                try {
                    JsonResponseModel response = responses.get(i);
                    if (response.error != null) {
                        throw new JsonException(request.getName() + ": " + response.error.message, response.error.code);
                    }
                    results[i]=parseResponse(response.result, request.getType());
                    request.invokeCallback(results[i]);
                } catch (Exception e) {
                    if(ex==null)
                    {
                        ex=e;
                    }
                    request.invokeCallback(e);
                }
                i++;
            }

        } catch (IOException e) {
            for (JsonRequest request : requests) {
                request.invokeCallback(e);
            }
            ex=e;
        } finally {

            requests.clear();
            try {
                if(reader!=null)
                {
                    reader.close();
                }
            } catch (IOException e) {}
            if(conn!=null)
            {
                conn.disconnect();
            }

            if(transactionCallback!=null)
            {
                if(ex==null)
                {
                    JsonRequest.invokeTransactionCallback(rpc, transactionCallback, results);
                }
                else
                {
                    JsonRequest.invokeTransactionCallback(rpc, transactionCallback, ex);
                }
            }

            parseTime=System.currentTimeMillis()-time;

            System.out.println("Transaction(" + connectionType + "): createRequests(" + (createTime) + "ms)"+
                    " connection&send(" + connectionTime + "ms)" +
                    " read(" + readTime  + "ms) parse(" + parseTime  + "ms)"+
                    " all(" + (System.currentTimeMillis()-startTime)  + "ms)");

        }

    }


}
