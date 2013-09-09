package com.jsonrpclib;

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

    public JsonBatchProgressObserver(JsonRpcImplementation rpc, JsonBatch batch) {
        this.rpc = rpc;
        this.batch = batch;
    }

    @Override
    public void progressTick() {
        progressTick(1);
    }

    @Override
    public synchronized void progressTick(float progress) {
        progress += progress;
        publishProgress();
    }


    public synchronized void progressTick(int i) {
        progress += i;
        publishProgress();
    }


    public void publishProgress() {
        if (batch != null && progress > 0) {
            rpc.getHandler().post(new JsonAsyncResult(batch, (int) (progress * 100 / max)));
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
