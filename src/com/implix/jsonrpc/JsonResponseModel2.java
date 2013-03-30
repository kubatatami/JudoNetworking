package com.implix.jsonrpc;

class JsonResponseModel2 extends JsonResponseModel  {

	JsonErrorModel error;
    Object cacheObject;

    JsonResponseModel2(Object cacheObject) {
        this.cacheObject = cacheObject;
    }
}
