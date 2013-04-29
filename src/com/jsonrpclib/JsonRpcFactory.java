package com.jsonrpclib;

import android.content.Context;
import com.google.gson22.GsonBuilder;

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
    public static JsonRpc getJsonRpc(Context context,String url)
    {
        return new JsonRpcImplementation(context,url);
    }

    /**
     * Create JsonRpc instance.
     * @param context Android context.
     * @param url Server url.
     * @param builder Customize gson builder.
     * @return JsonRpc instance.
     */
    public static JsonRpc getJsonRpc(Context context,String url,GsonBuilder builder)
    {
        return new JsonRpcImplementation(context,url, builder);
    }

    /**
     * Create JsonRpc instance.
     * @param context Android context.
     * @param url Server url.
     * @param apiKey Server api key.
     * @return JsonRpc instance.
     */
    public static JsonRpc getJsonRpc(Context context,String url,String apiKey)
    {
        return new JsonRpcImplementation(context,url, apiKey);
    }

    /**
     * Create JsonRpc instance.
     * @param context Android context.
     * @param url Server url.
     * @param apiKey Server api key.
     * @param builder Customize gson builder.
     * @return JsonRpc instance.
     */
    public static JsonRpc getJsonRpc(Context context,String url,String apiKey,GsonBuilder builder)
    {
        return new JsonRpcImplementation(context,url, apiKey, builder);
    }

}
