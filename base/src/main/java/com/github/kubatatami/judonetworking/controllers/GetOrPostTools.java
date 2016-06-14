package com.github.kubatatami.judonetworking.controllers;

import android.util.Pair;

import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 05.08.2013
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
public class GetOrPostTools {

    private GetOrPostTools() {
    }

    public static String createRequest(Request request, String apiKey, String apiKeyName) throws JudoException {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        if (apiKeyName != null && request.isApiKeyRequired()) {
            addGetParam(sb, apiKeyName, apiKey, true);
        }
        if (request.getArgs() != null) {
            if (request.getArgs().length != request.getParamNames().length) {
                throw new JudoException("Wrong param names.");
            }
            for (Object arg : request.getArgs()) {
                addGetParam(sb, request.getParamNames()[i], arg == null ? "" : arg.toString(), true);
                i++;
            }
        }
        return sb.toString();
    }

    public static void addGetParam(StringBuilder sb, Collection<? extends Pair> params, boolean encode) {
        for(Pair pair : params){
            addGetParam(sb, pair.first.toString(), pair.second.toString(), encode);
        }
    }

    public static void addGetParam(StringBuilder sb, String key, String value, boolean encode) {
        try {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(encode ? URLEncoder.encode(key, "UTF-8") : key);
            sb.append('=');
            sb.append(encode ? URLEncoder.encode(value, "UTF-8") : key);
        } catch (UnsupportedEncodingException e) {
            throw new JudoException(e);
        }
    }

}
