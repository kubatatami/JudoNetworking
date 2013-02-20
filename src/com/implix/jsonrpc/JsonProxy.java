package com.implix.jsonrpc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class JsonProxy implements InvocationHandler {

    Context context;
    JsonRpcImplementation rpc;
    int id = 0;
    boolean transaction;
    private List<JsonRequest> batchRequests = new ArrayList<JsonRequest>();

    public JsonProxy(Context context, JsonRpcImplementation rpc, boolean transaction) {
        this.rpc = rpc;
        this.context = context;
        this.transaction = transaction;
    }

    private Method getMethod(Object obj, String name) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        try {
            Method method = getMethod(this, m.getName());
            if (method != null) {
                return method.invoke(this, args);
            } else {
                String paramNames[] = null;
                String name = m.getName();
                Integer timeout = null;
                boolean async = false, notification = false;
                JsonMethod ann = m.getAnnotation(JsonMethod.class);
                if (ann != null) {
                    if (!ann.name().equals("")) {
                        name = ann.name();
                    }
                    if (ann.paramNames().length > 0) {
                        paramNames = ann.paramNames();
                    }
                    async = ann.async();
                    notification = ann.notification();
                    timeout = ann.timeout();
                }
                if (m.getReturnType().equals(Void.TYPE) && !async && notification) {
                    rpc.getJsonConnection().notify(name, paramNames, args, timeout, rpc.getApiKey());
                    return null;
                } else if (!async) {
                    return rpc.getJsonConnection().call(++id, name, paramNames, args, m.getGenericReturnType(), timeout, rpc.getApiKey());
                } else {
                    final JsonRequest request = callAsync(++id, name, paramNames, args, m.getGenericParameterTypes(), timeout, rpc.getApiKey());
                    if (transaction) {
                        batchRequests.add(request);
                        return null;
                    } else {
                        Thread thread = new Thread(request) ;
                        thread.start();

                        if (m.getReturnType().equals(Thread.class)) {
                            return thread;
                        } else {
                            return null;
                        }
                    }

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public JsonRequest callAsync(int id, String name, String[] params, Object[] args, Type[] types, Integer timeout, String apiKey) throws Exception {
        Object[] newArgs = null;
        JsonCallback<Object> callback = (JsonCallback<Object>) args[args.length - 1];
        if (args.length > 1) {
            newArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, newArgs, 0, args.length - 1);
        }
        Type type = ((ParameterizedType) types[args.length - 1]).getActualTypeArguments()[0];
        return new JsonRequest(id, rpc, callback, name, params, newArgs, type, timeout, apiKey);
    }

    public void callBatch(int timeout, final JsonBatch batch) {
        transaction = false;
        if (batchRequests.size() > 0) {

            try {
                List<JsonResponseModel2> responses = sendBatchRequest(timeout);
                parseBatchResponse(batch, responses);
            } catch (final Exception e) {
                rpc.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        batch.onError(e);
                    }
                });

            }

            batchRequests.clear();
        }
    }

    class BatchTask implements Runnable
    {
        private Integer timeout;
        private List<JsonRequest> requests;
        private Thread thread = new Thread(this);
        private List<JsonResponseModel2> response=null;
        private IOException ex=null;

        public BatchTask(Integer timeout, List<JsonRequest> requests) {
            this.timeout = timeout;
            this.requests = requests;
        }

        public List<JsonResponseModel2> getResponse() {
            return this.response;
        }

        public IOException getEx() {
            return ex;
        }

        @Override
        public void run() {
            try {
                this.response=rpc.getJsonConnection().callBatch(this.requests, this.timeout);
            } catch (IOException e) {
                this.ex=e;
            }
        }

        public void execute()
        {
            thread.start();
        }

        public void join() throws InterruptedException {
            thread.join();
        }

    }

    private static <T> List<List<T>> splitList(List<T> list, final int partsNo) {
        int i;
        final int partSize=Math.max(1,list.size()/partsNo);
        List<List<T>> parts = new ArrayList<List<T>>(partsNo);
        for (i = 0; i < partsNo-1; i++) {
            parts.add(list.subList(i * partSize, (i + 1) * partSize));
        }
        parts.add(list.subList(i*partSize,list.size()));
        return parts;
    }

    private List<JsonResponseModel2> sendBatchRequest(final Integer timeout) throws Exception {
        if (batchRequests.size() > 1 && (!rpc.isWifiOnly() || isWifi()) && rpc.getMaxBatchConnections()>0) {
            int connections = Math.min(batchRequests.size(), rpc.getMaxBatchConnections());
            List<List<JsonRequest>> requestParts=splitList(batchRequests, connections);
            List<JsonResponseModel2> response = new ArrayList<JsonResponseModel2>(batchRequests.size());
            List<BatchTask> tasks = new ArrayList<BatchTask>(connections);
            for(List<JsonRequest> requests : requestParts)
            {
                BatchTask task = new BatchTask(timeout,requests);
                tasks.add(task);
                task.execute();
            }
            for(BatchTask task : tasks)
            {
                task.join();
                if(task.ex!=null)
                {
                    throw task.getEx();
                }
                else
                {
                    response.addAll(task.getResponse());
                }
            }
            return response;
        } else {
            return rpc.getJsonConnection().callBatch(batchRequests, timeout);
        }
    }

    private void parseBatchResponse(JsonBatch batch, List<JsonResponseModel2> responses) {
        Object[] results = new Object[batchRequests.size()];
        Exception ex = null;
        int i = 0;
        for (JsonRequest request : batchRequests) {
            try {
                JsonResponseModel2 response = responses.get(i);
                if (response.error != null) {
                    throw new JsonException(request.getName() + ": " + response.error.message, response.error.code);
                }
                results[i] = parseResponse(response.result, request.getType());
                request.invokeCallback(results[i]);
            } catch (Exception e) {
                ex = e;
                request.invokeCallback(e);
            }
            i++;
        }

        if (ex == null) {
            JsonRequest.invokeTransactionCallback(rpc, batch, results);
        } else {
            JsonRequest.invokeTransactionCallback(rpc, batch, ex);
        }
    }

    private boolean isWifi() {
        final ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.getState() == NetworkInfo.State.CONNECTED;
    }

    private <T> T parseResponse(JsonElement result, Type type) {
        return rpc.getParser().fromJson(result, type);
    }

}
