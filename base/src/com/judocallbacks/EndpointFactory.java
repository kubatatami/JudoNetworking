package com.judocallbacks;

import android.content.Context;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 07.01.2013
 * Time: 12:50
 */
public class EndpointFactory {

    /**
     * Create Endpoint instance.
     *
     * @param context Android context.
     * @param url     Server url.
     * @return Endpoint instance.
     */
    public static Endpoint createEndpoint(Context context, ProtocolController protocolController, Connector connector, String url) {
        return new EndpointImplementation(context, protocolController, connector, url);
    }


}
