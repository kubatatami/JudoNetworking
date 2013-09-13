package com.jsonrpclib;

import android.util.Log;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 27.05.2013
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public class JsonBatchProgressObserver implements JsonProgressObserver {

    int max = JsonTimeStat.TICKS;
    float progress = 0;
    JsonBatch batch;
    JsonRpcImplementation rpc;
    List<JsonRequest> requestList;

    public JsonBatchProgressObserver(JsonRpcImplementation rpc, JsonBatch batch, List<JsonRequest> requestList) {
        this.rpc = rpc;
        this.batch = batch;
        this.requestList=requestList;
    }

    @Override
    public void progressTick() {
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


    public void publishProgress() {
        if (batch != null && progress > 0) {
            rpc.getHandler().post(new JsonAsyncResult(batch, (int) (progress * 100 / max)));
        }
        for(JsonRequest request : requestList)
        {
            if(request.getCallback()!=null)
            {
                rpc.getHandler().post(new JsonAsyncResult(request.getCallback(), (int) (progress * 100 / max)));
            }
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
