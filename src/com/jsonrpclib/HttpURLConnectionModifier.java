package com.jsonrpclib;

import java.net.HttpURLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 14.05.2013
 * Time: 11:46
 * To change this template use File | Settings | File Templates.
 */
public interface HttpURLConnectionModifier {

    void modify(HttpURLConnection connection);

}
