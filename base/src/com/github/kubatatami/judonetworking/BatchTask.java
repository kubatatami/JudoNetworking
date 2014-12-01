package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

class BatchTask implements Runnable {
    private final Integer timeout;
    private final List<Request> requests;
    private List<RequestResult> response = null;
    private JudoException ex = null;
    private final EndpointImplementation rpc;
    private ProgressObserver progressObserver;
    private Future future;

    public BatchTask(EndpointImplementation rpc, ProgressObserver progressObserver, Integer timeout, List<Request> requests) {
        this.rpc = rpc;
        this.progressObserver = progressObserver;
        this.timeout = timeout;
        this.requests = requests;
    }

    public List<RequestResult> getResponse() {
        return this.response;
    }

    public JudoException getEx() {
        return ex;
    }

    @Override
    public void run() {
        try {
            this.response = rpc.getRequestConnector().callBatch(this.requests, progressObserver, this.timeout);
        } catch (JudoException e) {
            this.ex = e;
        }
    }

    public void execute() {
        future = rpc.getExecutorService().submit(this);
        for(Request request : requests){
            request.setFuture(future);
        }
    }

    public void join() throws JudoException {
        try {
            future.get();
        } catch (Exception e) {
            throw new JudoException("Batch task exception", e);
        }
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