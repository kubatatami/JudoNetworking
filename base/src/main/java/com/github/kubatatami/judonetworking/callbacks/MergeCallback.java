package com.github.kubatatami.judonetworking.callbacks;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Kuba on 28/03/15.
 */
public class MergeCallback {

    int requests;
    int finishRequests = 0;
    boolean started = false;
    boolean canceled = false;
    Map<BaseCallback<?>, Integer> progressMap = new HashMap<>();
    Set<AsyncResult> asyncResultSet = new HashSet<>();
    Callback<?> finalCallback;

    public MergeCallback(int requests) {
        this.requests = requests;
    }

    public MergeCallback(int requests, Callback<?> finalCallback) {
        this.requests = requests;
        this.finalCallback = finalCallback;
    }

    public final void addStart(AsyncResult asyncResult) {
        if (canceled) {
            return;
        }
        if (!started) {
            started = true;
            onMergeStart();
        }
        asyncResultSet.add(asyncResult);
    }

    public final void addProgress(BaseCallback<?> callback, int progress) {
        if (canceled) {
            return;
        }
        progressMap.put(callback, progress);
        onMergeProgress(calculateProgress());
    }

    protected final int calculateProgress() {
        int progress = 0;
        for (int value : progressMap.values()) {
            progress += value;
        }
        return (int) ((float) progress / (float) requests);
    }

    public final void addSuccess() {
        if (canceled) {
            return;
        }
        finishRequests++;
        if (finishRequests == requests) {
            onMergeSuccess();
            onMergeFinish();
        }

    }

    public final void addError(JudoException e) {
        if (canceled) {
            return;
        }
        onMergeError(e);
        onMergeFinish();
        for (AsyncResult asyncResult : asyncResultSet) {
            asyncResult.cancel();
        }
        canceled = true;
    }


    protected void onMergeStart() {
        if (finalCallback != null) {
            finalCallback.onStart(null, null);
        }

    }

    protected void onMergeProgress(int progress) {
        if (finalCallback != null) {
            finalCallback.onProgress(progress);
        }
    }

    protected void onMergeSuccess() {
        if (finalCallback != null) {
            finalCallback.onSuccess(null);
        }
    }

    protected void onMergeError(JudoException e) {
        if (finalCallback != null) {
            finalCallback.onError(e);
        }
    }

    protected void onMergeFinish() {
        if (finalCallback != null) {
            finalCallback.onFinish();
        }
    }

}
