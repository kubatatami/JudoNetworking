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
import java.util.Collections;
import java.util.List;

class JsonProxy implements InvocationHandler {

    Context context;
    JsonRpcImplementation rpc;
    int id = 0;
    boolean batch = false;
    private final List<JsonRequest> batchRequests = new ArrayList<JsonRequest>();
    JsonBatchMode mode = JsonBatchMode.NONE;

    public JsonProxy(Context context, JsonRpcImplementation rpc, JsonBatchMode mode) {
        this.rpc = rpc;
        this.context = context;
        this.mode = mode;
        if (mode == JsonBatchMode.MANUAL) {
            batch = true;
        }
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
                int timeout = rpc.getJsonConnection().getMethodTimeout();
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
                    if(ann.timeout()!=0)
                    {
                        timeout = ann.timeout();
                    }
                }


                if (m.getReturnType().equals(Void.TYPE) && !async && notification) {
                    rpc.getJsonConnection().notify(name, paramNames, args, timeout, rpc.getApiKey());
                    return null;
                } else if (!async) {
                    return rpc.getJsonConnection().call(++id, name, paramNames, args, m.getGenericReturnType(), timeout, rpc.getApiKey());
                } else {
                    final JsonRequest request = callAsync(++id, name, paramNames, args, m.getGenericParameterTypes(), timeout, rpc.getApiKey());

                    if (batch) {
                        synchronized (batchRequests) {
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
                                    try {
                                        Thread.sleep(rpc.getAutoBatchTime());
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    JsonProxy.this.callBatch(new JsonBatch());
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
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public JsonRequest callAsync(int id, String name, String[] params, Object[] args, Type[] types, int timeout, String apiKey) throws Exception {
        Object[] newArgs = null;
        JsonCallback<Object> callback = (JsonCallback<Object>) args[args.length - 1];
        if (args.length > 1) {
            newArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, newArgs, 0, args.length - 1);
        }
        Type type = ((ParameterizedType) types[args.length - 1]).getActualTypeArguments()[0];
        return new JsonRequest(id, rpc, callback, name, params, newArgs, type, timeout, apiKey);
    }

    public void callBatch(final JsonBatch batch) {
        List<JsonRequest> batches = null;
        if (batchRequests.size() > 0) {


            synchronized (batchRequests) {
                batches = new ArrayList<JsonRequest>(batchRequests.size());
                batches.addAll(batchRequests);
                batchRequests.clear();
                this.batch = false;
            }
            try {
                List<JsonResponseModel2> responses = sendBatchRequest(batches);
                parseBatchResponse(batches, batch, responses);
            } catch (final Exception e) {
                rpc.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        batch.onError(e);
                    }
                });

            }


        } else {
            this.batch = false;
        }
    }

    class BatchTask implements Runnable {
        private Integer timeout;
        private List<JsonRequest> requests;
        private Thread thread = new Thread(this);
        private List<JsonResponseModel2> response = null;
        private Exception ex = null;

        public BatchTask(Integer timeout, List<JsonRequest> requests) {
            this.timeout = timeout;
            this.requests = requests;
        }

        public List<JsonResponseModel2> getResponse() {
            return this.response;
        }

        public Exception getEx() {
            return ex;
        }

        @Override
        public void run() {
            try {
                this.response = rpc.getJsonConnection().callBatch(this.requests, this.timeout);
            } catch (Exception e) {
                this.ex = e;
            }
        }

        public void execute() {
            thread.start();
        }

        public void join() throws InterruptedException {
            thread.join();
        }

    }

    private static <T> List<List<T>> splitList(List<T> list, final int partsNo) {
        int i;
        final int partSize = Math.max(1, list.size() / partsNo);
        List<List<T>> parts = new ArrayList<List<T>>(partsNo);
        for (i = 0; i < partsNo - 1; i++) {
            parts.add(list.subList(i * partSize, (i + 1) * partSize));
        }
        parts.add(list.subList(i * partSize, list.size()));
        return parts;
    }

    private int calculateTimeout(List<JsonRequest> batches)
    {
        int timeout=0;
        if(rpc.getTimeoutMode()==JsonBatchTimeoutMode.TIMEOUTS_SUM)
        {
            for(JsonRequest req : batches)
            {
                timeout+=req.getTimeout();
            }
        }
        else
        {
            for(JsonRequest req : batches)
            {
                timeout=Math.max(timeout, req.getTimeout());
            }
        }
        return timeout;
    }

    private List<JsonResponseModel2> sendBatchRequest(List<JsonRequest> batches) throws Exception {
        if (batches.size() > 1 && (!rpc.isWifiOnly() || isWifi()) && rpc.getMaxBatchConnections() > 0) {
            int connections = Math.min(batches.size(), rpc.getMaxBatchConnections());
            List<List<JsonRequest>> requestParts = splitList(batches, connections);
            List<JsonResponseModel2> response = new ArrayList<JsonResponseModel2>(batches.size());
            List<BatchTask> tasks = new ArrayList<BatchTask>(connections);
            for (List<JsonRequest> requests : requestParts) {

                BatchTask task = new BatchTask(calculateTimeout(requests), requests);
                tasks.add(task);
                task.execute();
            }
            for (BatchTask task : tasks) {
                task.join();
                if (task.ex != null) {
                    throw task.getEx();
                } else {
                    response.addAll(task.getResponse());
                }
            }
            return response;
        } else {

            return rpc.getJsonConnection().callBatch(batches, calculateTimeout(batches));
        }
    }

    private void parseBatchResponse(List<JsonRequest> requests, JsonBatch batch, List<JsonResponseModel2> responses) {
        Object[] results = new Object[requests.size()];
        Exception ex = null;
        int i = 0;
        for (JsonRequest request : requests) {
            try {
                JsonResponseModel2 response = responses.get(i);
                if (response.error != null) {
                    throw new JsonException(request.getName() + ": " + response.error.message, response.error.code);
                }
                if(request.getType()!=Void.class)
                {
                    results[i] = parseResponse(response.result, request.getType());
                }
                else
                {
                    results[i] = null;
                }
                request.invokeCallback(results[i]);
            } catch (Exception e) {
                ex = e;
                request.invokeCallback(new JsonException(request.getName(),e));
            }
            i++;
        }

        if (ex == null) {
            JsonRequest.invokeBatchCallback(rpc, batch, results);
        } else {
            JsonRequest.invokeBatchCallback(rpc, batch, ex);
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
