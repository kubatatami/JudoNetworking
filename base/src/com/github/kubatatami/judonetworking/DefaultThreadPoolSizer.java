package com.github.kubatatami.judonetworking;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by Kuba on 26/05/14.
 */
class DefaultThreadPoolSizer implements ThreadPoolSizer {

    public static final int DEFAULT_THREAD_COUNT = 2;

    @Override
    public int getThreadPoolSize(NetworkInfo info) {
        if (info == null || !info.isConnectedOrConnecting()) {
            return DEFAULT_THREAD_COUNT;
        }
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_ETHERNET:
                return 3;
            case ConnectivityManager.TYPE_MOBILE:
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        return 3;
                    case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        return 2;
                    case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return 1;
                    default:
                        return DEFAULT_THREAD_COUNT;
                }
            default:
                return DEFAULT_THREAD_COUNT;
        }
    }
}
