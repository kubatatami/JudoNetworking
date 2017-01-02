package com.github.kubatatami.judonetworking.stateful;

import com.github.kubatatami.judonetworking.CallbacksConnector;

/**
 * Created by Kuba on 11/10/15.
 */
public interface StatefulController {

    String getWho();

    void onConnectCallbacks(CallbacksConnector connector);

}
