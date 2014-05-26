package com.github.kubatatami.judonetworking;

import android.net.NetworkInfo;

/**
 * Created by Kuba on 26/05/14.
 */
public interface ThreadPoolSizer {

    int getThreadPoolSize(NetworkInfo activeNetworkInfo);

}
