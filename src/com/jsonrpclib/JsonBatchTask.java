package com.jsonrpclib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class JsonBatchTask implements Runnable {
    private final Integer timeout;
    private final List<JsonRequest> requests;
    private final Thread thread = new Thread(this);
    private List<JsonResult> response = null;
    private Exception ex = null;
    private final JsonRpcImplementation rpc;
    private JsonProgressObserver progressObserver;

    public JsonBatchTask(JsonRpcImplementation rpc, JsonProgressObserver progressObserver, Integer timeout, List<JsonRequest> requests) {
        this.rpc = rpc;
        this.progressObserver = progressObserver;
        this.timeout = timeout;
        this.requests = requests;
    }

    public List<JsonResult> getResponse() {
        return this.response;
    }

    public Exception getEx() {
        return ex;
    }

    @Override
    public void run() {
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            this.response = rpc.getJsonConnector().callBatch(this.requests, progressObserver, this.timeout);
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


    public static List<List<JsonRequest>> timeAssignRequests(List<JsonRequest> list, final int partsNo) {
        List<List<JsonRequest>> parts = new ArrayList<List<JsonRequest>>(partsNo);
        long[] weights = new long[partsNo];
        for (int i = 0; i < partsNo; i++) {
            parts.add(new ArrayList<JsonRequest>());
        }
        Collections.sort(list);
        for (JsonRequest req : list) {
            int i = getSmallestPart(weights);
            weights[i] += req.getWeight();
            parts.get(i).add(req);
        }

        return parts;
    }

    public static List<List<JsonRequest>> simpleAssignRequests(List<JsonRequest> list, final int partsNo) {
        int i;
        List<List<JsonRequest>> parts = new ArrayList<List<JsonRequest>>(partsNo);
        for (i = 0; i < partsNo; i++) {
            parts.add(new ArrayList<JsonRequest>());
        }

        Collections.sort(list, new Comparator<JsonRequest>() {
            @Override
            public int compare(JsonRequest lhs, JsonRequest rhs) {
                if (lhs.isHighPriority() && !rhs.isHighPriority()) {
                    return -1;
                } else if (!lhs.isHighPriority() && rhs.isHighPriority()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        i = 0;
        for (JsonRequest elem : list) {
            parts.get(i).add(elem);
            i++;
            if (i == partsNo) {
                i = 0;
            }
        }

        return parts;
    }

    private static int getSmallestPart(long[] parts) {
        int res = 0;

        for (int i = 0; i < parts.length; i++) {
            if (parts[i] < parts[res]) {
                res = i;
            }
        }
        return res;
    }

}