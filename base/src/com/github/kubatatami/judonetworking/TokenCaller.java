package com.github.kubatatami.judonetworking;

/**
 * Created by Kuba on 21/02/14.
 */
public abstract class TokenCaller {

    public abstract long doTokenRequest(Endpoint endpoint) throws Exception;

    public boolean checkIsTokenException(Exception exception) {
        return false;
    }

}
