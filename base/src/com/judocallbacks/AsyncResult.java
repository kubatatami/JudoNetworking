package com.judocallbacks;

class AsyncResult implements Runnable {
    private CallbackInterface<Object> callback;
    private BatchInterface<?> transaction;
    private Object result = null;
    private Object[] results = null;
    private Exception e = null;
    private int progress = 0;

    AsyncResult(BatchInterface<?> callback, Object results[]) {
        this.results = results;
        this.transaction = callback;
    }

    AsyncResult(BatchInterface<?> callback, int progress) {
        this.progress = progress;
        this.transaction = callback;
    }

    AsyncResult(BatchInterface<?> callback, Exception e) {
        this.e = e;
        this.transaction = callback;
    }

    AsyncResult(CallbackInterface<Object> callback, Object result) {
        this.result = result;
        this.callback = callback;
    }

    AsyncResult(CallbackInterface<Object> callback, int progress) {
        this.progress = progress;
        this.callback = callback;
    }

    AsyncResult(CallbackInterface<Object> callback, Exception e) {
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