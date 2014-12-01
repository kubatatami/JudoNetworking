package com.github.kubatatami.judonetworking.transports;

import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.utils.SecurityUtils;
import com.github.kubatatami.judonetworking.exceptions.HttpException;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;

/**
 * Created by Kuba on 16/11/14.
 */
public abstract class HttpTransportLayer extends TransportLayer {

    protected String authKey;
    protected String username;
    protected String password;
    protected SecurityUtils.DigestAuth digestAuth;
    protected static SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    protected int connectTimeout = 7500;
    protected int methodTimeout = 5000;
    protected boolean followRedirection = true;


    public void setFollowRedirection(boolean followRedirection) {
        this.followRedirection = followRedirection;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getMethodTimeout() {
        return methodTimeout;
    }

    public void setMethodTimeout(int methodTimeout) {
        this.methodTimeout = methodTimeout;
    }



    protected void handleHttpException(ProtocolController protocolController, int code, String message) throws JudoException {
        protocolController.parseError(code, message);
        throw new HttpException(message + "(" + code + ")", code);
    }

    public void setDigestAuthentication(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public void setBasicAuthentication(final String username, final String password) {
        if(username == null || password == null){
            authKey=null;
        }else {
            authKey = SecurityUtils.getBasicAuthHeader(username, password);
        }
    }

    public void setBasicAuthentication(final String hash) {
        if(hash == null){
            authKey=null;
        }else {
            authKey = "Basic " + hash;
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface HttpMethod {
        String value();
    }

}
