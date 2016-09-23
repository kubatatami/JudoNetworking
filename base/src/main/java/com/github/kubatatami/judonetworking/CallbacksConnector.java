package com.github.kubatatami.judonetworking;

import com.github.kubatatami.judonetworking.callbacks.BaseCallback;
import com.github.kubatatami.judonetworking.stateful.StatefulCache;
import com.github.kubatatami.judonetworking.stateful.StatefulController;

public class CallbacksConnector {

    private StatefulController controller;

    public CallbacksConnector(StatefulController controller) {
        this.controller = controller;
    }

    public boolean connectCallback(BaseCallback<?>... callbacks) {
        return StatefulCache.connectControllerCallbacks(controller, callbacks);
    }

    public boolean connectCallback(int id, BaseCallback<?> callback) {
        return StatefulCache.connectControllerCallback(controller, id, callback);
    }

}
