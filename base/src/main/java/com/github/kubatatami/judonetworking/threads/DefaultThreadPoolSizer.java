package com.github.kubatatami.judonetworking.threads;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by Kuba on 26/05/14.
 */
public class DefaultThreadPoolSizer implements ThreadPoolSizer {

    public static final int DEFAULT_CONNECTIONS = 2;

    @Override
    public int getThreadPoolSize(Context context, NetworkInfo info) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            return 6;
        }
        if (info == null || !info.isConnectedOrConnecting()) {
            return DEFAULT_CONNECTIONS;
        }
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_ETHERNET:
                return 5;
            case ConnectivityManager.TYPE_MOBILE:
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        return 4;
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
                        return DEFAULT_CONNECTIONS;
                }
            default:
                return DEFAULT_CONNECTIONS;
        }
    }
}
