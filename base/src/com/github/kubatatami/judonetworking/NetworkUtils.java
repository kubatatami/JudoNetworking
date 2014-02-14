package com.github.kubatatami.judonetworking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jbogacki on 03.01.2014.
 */
public class NetworkUtils {

    protected static Set<NetworkStateListener> networkStateListeners = new HashSet<NetworkStateListener>();

    protected NetworkUtils() {
    }

    protected static BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (NetworkStateListener networkStateListener : networkStateListeners) {
                networkStateListener.onNetworkStateChange(isNetworkAvailable(context));
            }
        }
    };

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public static boolean isWifi(Context context) {
        final ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.getState() == NetworkInfo.State.CONNECTED;
    }

    public static void addNetworkStateListener(Context context, NetworkStateListener networkStateListener) {
        networkStateListeners.add(networkStateListener);
        if (networkStateListeners.size() == 1) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.getApplicationContext().registerReceiver(broadcastReceiver, filter);
        }
    }

    public static void removeNetworkStateListener(Context context, NetworkStateListener networkStateListener) {
        networkStateListeners.remove(networkStateListener);
        if (networkStateListeners.size() == 0) {
            context.getApplicationContext().unregisterReceiver(broadcastReceiver);
        }
    }

    public static interface NetworkStateListener {

        public void onNetworkStateChange(boolean networkAvailable);

    }

}
