package com.jsonrpclib;

class JsonAsyncResult implements Runnable {
    private JsonCallbackInterface<Object> callback;
    private JsonBatchInterface<?> transaction;
    private Object result = null;
    private Object[] results = null;
    private Exception e = null;
    private int progress = 0;

    JsonAsyncResult(JsonBatchInterface<?> callback, Object results[]) {
        this.results = results;
        this.transaction = callback;
    }

    JsonAsyncResult(JsonBatchInterface<?> callback, int progress) {
        this.progress = progress;
        this.transaction = callback;
    }

    JsonAsyncResult(JsonBatchInterface<?> callback, Exception e) {
        this.e = e;
        this.transaction = callback;
    }

    JsonAsyncResult(JsonCallbackInterface<Object> callback, Object result) {
        this.result = result;
        this.callback = callback;
    }

    JsonAsyncResult(JsonCallbackInterface<Object> callback, int progress) {
        this.progress = progress;
        this.callback = callback;
    }

    JsonAsyncResult(JsonCallbackInterface<Object> callback, Exception e) {
        this.e = e;
        this.callback = callback;
    }

    @Override
    public void run() {
        if (callback != null) {
            if (e != null) {
                callback.onError(e);
            } else if (progress != 0) {
                callback.onProgress(progress);
            } else {
                callback.onFinish(result);
            }
        } else {
            if (e != null) {
                transaction.onError(e);
            } else if (progress != 0) {
                transaction.onProgress(progress);
            } else {
                transaction.onFinish(results);
            }
        }
    }
}