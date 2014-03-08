package com.github.kubatatami.judonetworking;

class VirtualCallback implements CallbackInterface {
    private int id;
    private RequestResult result;

    VirtualCallback(int id) {
        this.id = id;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onFinish(Object result) {
        this.result = new RequestSuccessResult(id, result);
    }

    @Override
    public void onError(Exception e) {
        this.result = new ErrorResult(id, e);
    }

    @Override
    public void onProgress(int progress) {
        throw new IllegalAccessError("Virtual server can't invoke onProgress");
    }

    public RequestResult getResult() {
        return result;
    }
}