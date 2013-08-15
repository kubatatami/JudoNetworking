package com.jsonrpclib;

class JsonRequestModel {

    private final String method;
    private final Object params;
    private final Integer id;


    public JsonRequestModel(String method, Object params, Integer id) {
        super();
        this.method = method;
        this.params = params;
        this.id = id;
    }


}
