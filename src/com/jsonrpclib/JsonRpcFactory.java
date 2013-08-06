package com.jsonrpclib;

import android.content.Context;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 12:50
 *
 */
public class JsonRpcFactory {

    /**
     * Create JsonRpc instance.
     * @param context Android context.
     * @param url Server url.
     * @return JsonRpc instance.
     */
    public static JsonRpc getJsonRpc(Context context, ProtocolController protocolController, JsonConnection connection,String url)
    {
        return new JsonRpcImplementation(context, protocolController,connection,url);
    }



    /**
     * Create JsonRpc instance.
     * @param context Android context.
     * @param url Server url.
     * @param apiKey Server api key.
     * @return JsonRpc instance.
     */
    public static JsonRpc getJsonRpc(Context context,ProtocolController protocolController, JsonConnection connection,String url,String apiKey)
    {
        return new JsonRpcImplementation(context, protocolController,connection,url, apiKey);
    }



}
