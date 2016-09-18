package com.github.kubatatami.judonetworking.controllers;

import android.util.Pair;

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
