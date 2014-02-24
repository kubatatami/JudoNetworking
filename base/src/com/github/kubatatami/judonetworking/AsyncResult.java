package com.github.kubatatami.judonetworking;

class AsyncResult implements Runnable {
    private CallbackInterface<Object> callback;
    private BatchInterface<?> transaction;
    private Object result = null;
    private Object[] results = null;
    private Exception e = null;
    private int progress = 0;
    private final Type type;

    enum Type {
        RESULT, ERROR, PROGRESS
    }

    AsyncResult(BatchInterface<?> callback, Object results[]) {
        this.results = results;
        this.transaction = callback;
        type = Type.RESULT;
    }

    AsyncResult(BatchInterface<?> callback, int progress) {
        this.progress = progress;
        this.transaction = callback;
        type = Type.PROGRESS;
    }

    AsyncResult(BatchInterface<?> callback, Exception e) {
        this.e = e;
        this.transaction = callback;
        type = Type.ERROR;
    }

    AsyncResult(CallbackInterface<Object> callback, Object result) {
        this.result = result;
        this.callback = callback;
        type = Type.RESULT;
    }

    AsyncResult(CallbackInterface<Object> callback, int progress) {
        this.progress = progress;
        this.callback = callback;
        type = Type.PROGRESS;
    }

    AsyncResult(CallbackInterface<Object> callback, Exception e) {
        this.e = e;
        this.callback = callback;
        type = Type.ERROR;
    }

    @Override
    public void run() {
        if (callback != null) {
            switch (type) {
                case RESULT:
                    callback.onFinish(result);
                    break;
                case ERROR:
                    callback.onError(e);
                    break;
                case PROGRESS:
                    callback.onProgress(progress);
                    break;
            }
        } else {
            switch (type) {
                case RESULT:
                    transaction.onFinish(results);
                    break;
                case ERROR:
                    transaction.onError(e);
                    break;
                case PROGRESS:
                    transaction.onProgress(progress);
                    break;
            }
        }
    }
}