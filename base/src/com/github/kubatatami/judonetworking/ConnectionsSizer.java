package com.github.kubatatami.judonetworking;

import android.net.NetworkInfo;

/**
 * Created by Kuba on 26/05/14.
 */
public interface ConnectionsSizer {

    int getThreadPoolSize(NetworkInfo activeNetworkInfo);

}
