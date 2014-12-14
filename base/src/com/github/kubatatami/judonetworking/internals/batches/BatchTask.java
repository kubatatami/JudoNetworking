package com.github.kubatatami.judonetworking.internals.batches;

import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.EndpointImpl;
import com.github.kubatatami.judonetworking.internals.ProgressObserver;
import com.github.kubatatami.judonetworking.internals.requests.RequestImpl;
import com.github.kubatatami.judonetworking.internals.results.RequestResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

public class BatchTask implements Runnable {
    private final Integer timeout;
    private final List<RequestImpl> requests;
    private List<RequestResult> response = null;
    private JudoException ex = null;
    private final EndpointImpl rpc;
    private ProgressObserver progressObserver;
    private Future future;

    public BatchTask(EndpointImpl rpc, ProgressObserver progressObserver, Integer timeout, List<RequestImpl> requests) {
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
        for(RequestImpl request : requests){
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


    public static List<List<RequestImpl>> timeAssignRequests(List<RequestImpl> list, final int partsNo) {
        List<List<RequestImpl>> parts = new ArrayList<List<RequestImpl>>(partsNo);
        long[] weights = new long[partsNo];
        for (int i = 0; i < partsNo; i++) {
            parts.add(new ArrayList<RequestImpl>());
        }
        Collections.sort(list);
        for (RequestImpl req : list) {
            int i = getSmallestPart(weights);
            weights[i] += req.getWeight();
            parts.get(i).add(req);
        }

        return parts;
    }

    public static List<List<RequestImpl>> simpleAssignRequests(List<RequestImpl> list, final int partsNo) {
        int i;
        List<List<RequestImpl>> parts = new ArrayList<List<RequestImpl>>(partsNo);
        for (i = 0; i < partsNo; i++) {
            parts.add(new ArrayList<RequestImpl>());
        }

        Collections.sort(list, new Comparator<RequestImpl>() {
            @Override
            public int compare(RequestImpl lhs, RequestImpl rhs) {
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
        for (RequestImpl elem : list) {
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