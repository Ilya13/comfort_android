package com.example.comfort.monitors;

import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

import com.example.comfort.models.NetworkStatus;

import java.net.InetAddress;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class NetworkMonitor extends ConnectivityManager.NetworkCallback {
    private static final String INTERNAL_IP_MASK = "192.168.31.";
    public static final BehaviorSubject<NetworkStatus> NetworkStatus$ = BehaviorSubject.create();
    private static int networkStatus = -1;
    private static String networkIp = "";

    private static void clear() {
        networkStatus = -1;
        networkIp = "";
        emit();
    }

    private static void emit() {
        if (networkStatus == -1 || networkIp.equals("")) {
            NetworkStatus$.onNext(NetworkStatus.None);
        } else {
            NetworkStatus$.onNext(
                    networkStatus == NetworkCapabilities.TRANSPORT_WIFI && networkIp.startsWith(INTERNAL_IP_MASK) ?
                            NetworkStatus.Internal :
                            NetworkStatus.External
            );
        }
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        clear();
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
        clear();
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            networkStatus = -1;
        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            networkStatus = NetworkCapabilities.TRANSPORT_WIFI;
        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            networkStatus = NetworkCapabilities.TRANSPORT_CELLULAR;
        }
        emit();
    }

    @Override
    public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
        for (LinkAddress linkAddress: linkProperties.getLinkAddresses()) {
            InetAddress inetAddress = linkAddress.getAddress();
            if (!inetAddress.isLoopbackAddress()) {
                String hostAddress = inetAddress.getHostAddress();
                boolean isIPv4 = hostAddress.indexOf(':')<0;
                if (isIPv4) {
                    networkIp = hostAddress;
                }
            }
        }
        emit();
    }
}
