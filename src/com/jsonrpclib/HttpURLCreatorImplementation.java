package com.jsonrpclib;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 24.05.2013
 * Time: 12:07
 * To change this template use File | Settings | File Templates.
 */
public class HttpURLCreatorImplementation implements HttpURLCreator {

    @Override
    public HttpURLConnection create(String url) throws IOException {

        return (HttpURLConnection) new URL(url).openConnection();
    }

}
