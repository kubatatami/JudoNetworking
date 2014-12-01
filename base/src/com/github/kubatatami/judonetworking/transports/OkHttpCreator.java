package com.github.kubatatami.judonetworking.transports;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Kuba on 09/09/14.
 */
public class OkHttpCreator implements HttpUrlConnectionTransportLayer.HttpURLCreator {

    protected OkHttpClient client = new OkHttpClient();
    protected OkUrlFactory factory = new OkUrlFactory(client);

    @Override
    public HttpURLConnection create(String url) throws IOException {
        return factory.open(new URL(url));
    }

    public OkHttpClient getClient() {
        return client;
    }
}
