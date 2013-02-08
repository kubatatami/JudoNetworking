package com.implix.jsonrpc;

import android.content.Context;
import com.google.gson.GsonBuilder;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
public class JsonRpcFactory {

    public static JsonRpc getJsonRpc(Context context,String url)
    {
        return new JsonRpcImplementation(url);
    }

    public static JsonRpc getJsonRpc(Context context,String url,GsonBuilder builder)
    {
        return new JsonRpcImplementation(url, builder);
    }


    public static JsonRpc getJsonRpc(Context context,String url,String apiKey)
    {
        return new JsonRpcImplementation(url, apiKey);
    }


    public static JsonRpc getJsonRpc(Context context,String url,String apiKey,GsonBuilder builder)
    {
        return new JsonRpcImplementation(url, apiKey, builder);
    }

}
