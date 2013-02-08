package com.implix.jsonrpc;

 class JsonRequestModel {

	String method;
	Object params;
	Integer id;


	public JsonRequestModel(String method, Object params, Integer id) {
		super();
		this.method = method;
		this.params = params;
        this.id=id;
	}
	
	
}
