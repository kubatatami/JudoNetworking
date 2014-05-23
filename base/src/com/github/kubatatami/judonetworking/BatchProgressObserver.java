package com.github.kubatatami.judonetworking;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.05.2013
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public class BatchProgressObserver implements ProgressObserver {

    int max = TimeStat.TICKS;
    float progress = 0;
    RequestProxy requestProxy;
    EndpointImplementation rpc;
    List<Request> requestList;
    int lastProgress = 0;

    public BatchProgressObserver(EndpointImplementation rpc, RequestProxy requestProxy, List<Request> requestList) {
        this.rpc = rpc;
        this.requestProxy = requestProxy;
        this.requestList = requestList;
    }

    @Override
    public synchronized void clearProgress() {
        this.progress = 0;
        this.lastProgress = 0;
        publishProgress();
    }

    @Override
    public synchronized void progressTick() {
        progressTick(1.0f);
    }

    @Override
    public synchronized void progressTick(float progress) {
        this.progress += progress;
        publishProgress();
    }


    public void progressTick(int i) {
        progressTick((float) i);
    }


    public synchronized void publishProgress() {
        int percentProgress = (int) (progress * 100 / max);
        if (lastProgress < percentProgress) {
            lastProgress = percentProgress;
            if (requestProxy.getBatchCallback() != null && progress > 0) {
                Request.invokeBatchCallbackProgress(rpc, requestProxy, percentProgress);
            }
            rpc.getHandler().post(new AsyncResultSender(requestList, percentProgress));
        }
    }


    @Override
    public void setMaxProgress(int max) {
        this.max = max;
    }

    @Override
    public int getMaxProgress() {
        return max;
    }
}
