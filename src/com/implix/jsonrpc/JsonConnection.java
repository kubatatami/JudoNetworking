package com.implix.jsonrpc;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 24.03.2013
 * Time: 21:53
 * To change this template use File | Settings | File Templates.
 */
public interface JsonConnection {

    public <T> T call(int id, String name, String[] params, Object[] args, Type type,
                      int timeout, String apiKey, boolean cachable, int cacheLifeTime, int cacheSize) throws Exception;

    public void notify(String name, String[] params, Object[] args, Integer timeout, String apiKey) throws Exception;

    public List<JsonResponseModel2> callBatch(List<JsonRequest> requests, Integer timeout) throws Exception;

}
