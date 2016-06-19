package com.github.kubatatami.judonetworking.transports;

import com.github.kubatatami.judonetworking.AsyncResult;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

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
            await();
            if (accessToken != null && tokenType != null) {
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

    private void await() throws IOException {
        try {
            if (tokenAsyncResult != null) {
                tokenAsyncResult.await();
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    protected Authenticator oAuthAuthenticator = new Authenticator() {
        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            if (lastTokenValid && canDoTokenRequest()) {
                boolean createToken = false;
                if (tokenAsyncResult == null || tokenAsyncResult.isDone()) {
                    tokenAsyncResult = doTokenRequest();
                    createToken = true;
                }
                await();
                if (accessToken != null) {
                    if (createToken) {
                        lastTokenValid = false;
                    }
                    return response.request();
                }
            }
            return null;
        }
    };

    public void prepareOkHttpToOAuth(OkHttpClient.Builder okHttpClient) {
        okHttpClient.networkInterceptors().add(oAuthInterceptor);
        okHttpClient.authenticator(oAuthAuthenticator);
    }

    public void setOAuthToken(String tokenType, String accessToken) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
    }

    protected abstract AsyncResult doTokenRequest();

    protected abstract boolean canDoTokenRequest();

}
