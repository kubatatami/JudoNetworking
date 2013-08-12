package com.jsonrpclib.controllers;

import com.google.gson22.GsonBuilder;
import com.jsonrpclib.JsonRequestInterface;
import com.jsonrpclib.ProtocolController;
import com.jsonrpclib.JsonRequest;
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
abstract class JsonController  {

    public static String createRequest(JsonRequestInterface request, String apiKey) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        int i = 0;
        if (apiKey != null && request.getParamNames().length - 1 == request.getArgs().length) {
            nameValuePairs.add(new BasicNameValuePair(request.getParamNames()[0], apiKey));
            i++;
        }

        for (Object arg : request.getArgs()) {
            nameValuePairs.add(new BasicNameValuePair(request.getParamNames()[i], arg==null ? "" : arg.toString()));
            i++;
        }


        return URLEncodedUtils.format(nameValuePairs, HTTP.UTF_8).replaceAll("\\+", "%20");
    }

}
