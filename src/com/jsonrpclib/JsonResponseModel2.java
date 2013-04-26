package com.jsonrpclib;

class JsonResponseModel2 extends JsonResponseModel  {

	JsonErrorModel error;
    final Object cacheObject;

    JsonResponseModel2(Object cacheObject) {
        this.cacheObject = cacheObject;
    }
}
