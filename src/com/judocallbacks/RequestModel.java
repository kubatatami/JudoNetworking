package com.judocallbacks;

class RequestModel {

    private final String method;
    private final Object params;
    private final Integer id;


    public RequestModel(String method, Object params, Integer id) {
        super();
        this.method = method;
        this.params = params;
        this.id = id;
    }


}
