package com.jsonrpclib;

 class JsonRequestModel2 extends JsonRequestModel {
	final String jsonrpc="2.0";


     public JsonRequestModel2(String method, Object params, Integer id) {
         super(method, params, id);
     }
 }
