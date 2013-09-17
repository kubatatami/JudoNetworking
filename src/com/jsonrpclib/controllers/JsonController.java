package com.jsonrpclib.controllers;

import com.jsonrpclib.JsonRequestInterface;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
abstract class JsonController {

    public static String createRequest(JsonRequestInterface request, String apiKey, String apiKeyName) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        int i = 0;
        if (apiKeyName != null) {
            nameValuePairs.add(new BasicNameValuePair(apiKeyName, apiKey));
        }

        for (Object arg : request.getArgs()) {
            nameValuePairs.add(new BasicNameValuePair(request.getParamNames()[i], arg == null ? "" : arg.toString()));
            i++;
        }


        return URLEncodedUtils.format(nameValuePairs, HTTP.UTF_8).replaceAll("\\+", "%20");
    }

}
