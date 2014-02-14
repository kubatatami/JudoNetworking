package com.github.kubatatami.judonetworking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class BatchTask implements Runnable {
    private final Integer timeout;
    private final List<Request> requests;
    private final Thread thread = new Thread(this);
    private List<RequestResult> response = null;
    private Exception ex = null;
    private final EndpointImplementation rpc;
    private ProgressObserver progressObserver;

    public BatchTask(EndpointImplementation rpc, ProgressObserver progressObserver, Integer timeout, List<Request> requests) {
        this.rpc = rpc;
        this.progressObserver = progressObserver;
        this.timeout = timeout;
        this.requests = requests;
    }

    public List<RequestResult> getResponse() {
        return this.response;
    }

    public Exception getEx() {
        return ex;
    }

    @Override
    public void run() {
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            this.response = rpc.getRequestConnector().callBatch(this.requests, progressObserver, this.timeout);
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


    public static List<List<Request>> timeAssignRequests(List<Request> list, final int partsNo) {
        List<List<Request>> parts = new ArrayList<List<Request>>(partsNo);
        long[] weights = new long[partsNo];
        for (int i = 0; i < partsNo; i++) {
            parts.add(new ArrayList<Request>());
        }
        Collections.sort(list);
        for (Request req : list) {
            int i = getSmallestPart(weights);
            weights[i] += req.getWeight();
            parts.get(i).add(req);
        }

        return parts;
    }

    public static List<List<Request>> simpleAssignRequests(List<Request> list, final int partsNo) {
        int i;
        List<List<Request>> parts = new ArrayList<List<Request>>(partsNo);
        for (i = 0; i < partsNo; i++) {
            parts.add(new ArrayList<Request>());
        }

        Collections.sort(list, new Comparator<Request>() {
            @Override
            public int compare(Request lhs, Request rhs) {
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
        for (Request elem : list) {
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