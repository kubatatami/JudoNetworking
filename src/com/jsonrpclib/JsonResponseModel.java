package com.jsonrpclib;


import com.google.gson22.JsonElement;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 08.02.2013
 * Time: 09:20
 *
 */
class JsonResponseModel implements Comparable<JsonResponseModel2>{
    Integer id;
    JsonElement result;

    @Override
    public int compareTo(JsonResponseModel2 model) {
        return this.id.compareTo(model.id);
    }
}
