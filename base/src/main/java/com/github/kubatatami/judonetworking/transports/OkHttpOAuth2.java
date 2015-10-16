package com.github.kubatatami.judonetworking.transports;

import com.github.kubatatami.judonetworking.AsyncResult;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.Proxy;

/**
 * Created by Kuba on 16/10/15.
 */
public abstract class OkHttpOAuth2 {

    protected String tokenType;

    protected String accessToken;

    protected AsyncResult tokenAsyncResult;

    protected boolean lastTokenValid = true;

    protected Interceptor oAuthInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (accessToken != null && tokenType != null) {
                try {
                    if (tokenAsyncResult != null) {
                        tokenAsyncResult.await();
                    }
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
                request = request.newBuilder()
                        .header("Authorization", tokenType + " " + accessToken).build();
            }
            Response response = chain.proceed(request);
            if (response.isSuccessful()) {
                lastTokenValid = true;
            }
            return response;
        }
    };

    protected Authenticator oAuthAuthenticator = new Authenticator() {
        @Override
        public Request authenticate(Proxy proxy, Response response) throws IOException {
            if (lastTokenValid && canDoTokenRequest()) {
                boolean createToken = false;
                if (tokenAsyncResult == null || tokenAsyncResult.isDone()) {
                    tokenAsyncResult = doTokenRequest();
                    createToken = true;
                }
                try {
                    tokenAsyncResult.await();
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
                if (accessToken != null) {
                    if (createToken) {
                        lastTokenValid = false;
                    }
                    return response.request();
                }
            }
            return null;
        }

        @Override
        public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
            return null;
        }
    };

    public void prepareOkHttpToOAuth(OkHttpClient okHttpClient) {
        okHttpClient.networkInterceptors().add(oAuthInterceptor);
        okHttpClient.setAuthenticator(oAuthAuthenticator);
    }

    public void setOAuthToken(String tokenType, String accessToken) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
    }

    protected abstract AsyncResult doTokenRequest();

    protected abstract boolean canDoTokenRequest();

}
