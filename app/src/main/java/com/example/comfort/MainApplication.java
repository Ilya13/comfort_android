package com.example.comfort;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;

import com.example.comfort.monitors.NetworkMonitor;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ConnectivityManager connectivityDispatcher = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityDispatcher != null) {
            NetworkRequest networkRequest = new NetworkRequest.Builder().build();
            connectivityDispatcher.registerNetworkCallback(networkRequest, new NetworkMonitor());
        }
    }
}
