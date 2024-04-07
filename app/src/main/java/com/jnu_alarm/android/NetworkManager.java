package com.jnu_alarm.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkManager {

    public static boolean checkNetworkState(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities actNetwork = connectivityManager.getNetworkCapabilities(network);
        if (actNetwork == null) return false;

        return actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }
}
