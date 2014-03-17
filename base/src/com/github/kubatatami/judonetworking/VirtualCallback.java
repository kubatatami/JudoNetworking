package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.exceptions.JudoException;

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
    public void onSuccess(Object result) {
        this.result = new RequestSuccessResult(id, result);
    }

    @Override
    public void onError(JudoException e) {
        this.result = new ErrorResult(id, e);
    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onProgress(int progress) {
        throw new IllegalAccessError("Virtual server can't invoke onProgress");
    }

    public RequestResult getResult() {
        return result;
    }
}