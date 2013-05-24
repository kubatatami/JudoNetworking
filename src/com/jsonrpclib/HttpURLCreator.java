package com.jsonrpclib;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 24.05.2013
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */
public interface HttpURLCreator {

    public HttpURLConnection create(String url) throws IOException;

}
