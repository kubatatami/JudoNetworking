package com.implix.jsonrpc;

import com.google.gson.JsonElement;


class JsonResponseModel2 extends JsonResponseModel  {

	JsonErrorModel error;
    Object cacheObject;

    JsonResponseModel2(Object cacheObject) {
        this.cacheObject = cacheObject;
    }
}
