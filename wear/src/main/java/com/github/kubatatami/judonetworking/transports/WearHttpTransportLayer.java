package com.github.kubatatami.judonetworking.transports;

import android.content.Context;

import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.stats.TimeStat;
import com.github.kubatatami.judonetworking.internals.wear.DataLayerHttpTransportLayer;
import com.github.kubatatami.judonetworking.utils.NetworkUtils;

import java.lang.reflect.Method;

/**
 * Created by Kuba on 04/10/15.
 */
public class WearHttpTransportLayer extends HttpTransportLayer {

    Context context;

    OkHttpTransportLayer okHttpTransportLayer = new OkHttpTransportLayer();

    DataLayerHttpTransportLayer dataLayerHttpTransportLayer;

    public WearHttpTransportLayer(Context context) {
        this.context = context;
        dataLayerHttpTransportLayer = new DataLayerHttpTransportLayer(context);
    }

    protected HttpTransportLayer getTransportLayer() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            return okHttpTransportLayer;
        } else {
            return dataLayerHttpTransportLayer;
        }
    }

    @Override
    public Connection send(String requestName, ProtocolController protocolController, ProtocolController.RequestInfo requestInfo, int timeout, TimeStat timeStat, int debugFlags, Method method, CacheInfo cacheInfo) throws JudoException {
        return getTransportLayer().send(requestName, protocolController, requestInfo, timeout, timeStat, debugFlags, method, cacheInfo);
    }

    @Override
    public void setMaxConnections(int max) {
        okHttpTransportLayer.setMaxConnections(max);
        dataLayerHttpTransportLayer.setMaxConnections(max);
    }

    @Override
    public void setFollowRedirection(boolean followRedirection) {
        okHttpTransportLayer.setFollowRedirection(followRedirection);
        dataLayerHttpTransportLayer.setFollowRedirection(followRedirection);
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        okHttpTransportLayer.setConnectTimeout(connectTimeout);
        dataLayerHttpTransportLayer.setConnectTimeout(connectTimeout);
    }

    @Override
    public int getMethodTimeout() {
        return okHttpTransportLayer.getMethodTimeout();
    }

    @Override
    public void setMethodTimeout(int methodTimeout) {
        okHttpTransportLayer.setMethodTimeout(connectTimeout);
        dataLayerHttpTransportLayer.setMethodTimeout(connectTimeout);
    }

    @Override
    public void setDigestAuthentication(String username, String password) {
        okHttpTransportLayer.setDigestAuthentication(username, password);
        dataLayerHttpTransportLayer.setDigestAuthentication(username, password);
    }

    @Override
    public void setBasicAuthentication(String username, String password) {
        okHttpTransportLayer.setBasicAuthentication(username, password);
        dataLayerHttpTransportLayer.setBasicAuthentication(username, password);
    }

    @Override
    public void setBasicAuthentication(String hash) {
        okHttpTransportLayer.setBasicAuthentication(hash);
        dataLayerHttpTransportLayer.setBasicAuthentication(hash);
    }
}
