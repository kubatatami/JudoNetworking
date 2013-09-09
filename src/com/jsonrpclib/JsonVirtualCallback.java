package com.jsonrpclib;

class JsonVirtualCallback implements JsonCallbackInterface {
    private int id;
    private JsonResult result;

    JsonVirtualCallback(int id) {
        this.id = id;
    }

    @Override
    public void onFinish(Object result) {
        this.result = new JsonSuccessResult(id, result);
    }

    @Override
    public void onError(Exception e) {
        this.result = new JsonErrorResult(id, e);
    }

    @Override
    public void onProgress(int progress) {
        throw new IllegalAccessError("Virtual server can't invoke onProgress");
    }

    public JsonResult getResult() {
        return result;
    }
}