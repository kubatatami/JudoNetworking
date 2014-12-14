package com.github.kubatatami.judonetworking;

import android.content.Context;

import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.internals.EndpointImpl;
import com.github.kubatatami.judonetworking.transports.TransportLayer;

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
    public static Endpoint createEndpoint(Context context, ProtocolController protocolController, TransportLayer transportLayer, String url) {
        return new EndpointImpl(context, protocolController, transportLayer, url);
    }

    public static EndpointClassic createEndpointClassic(Context context, ProtocolController protocolController, TransportLayer transportLayer) {
        return new EndpointImpl(context, protocolController, transportLayer, null);
    }


}
