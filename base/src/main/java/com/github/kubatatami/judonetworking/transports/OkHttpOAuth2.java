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

    protected Interceptor oAuthInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            synchronized (this) {
                Request request = chain.request();
                if (accessToken != null && tokenType != null) {
                    request = request.newBuilder()
                            .header("Authorization", tokenType + " " + accessToken).build();
                }
                return chain.proceed(request);
            }
        }
    };

    protected Authenticator oAuthAuthenticator = new Authenticator() {
        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            String prevAccessToken = accessToken;
            synchronized (this) {
                if (canDoTokenRequest()) {
                    if (prevAccessToken.equals(accessToken)) {
                        try {
                            doTokenRequest().await();
                        } catch (InterruptedException e) {
                            throw new IOException(e);
                        }
                    }
                    if (accessToken != null) {
                        return response.request();
                    }
                }
                return null;
            }
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
