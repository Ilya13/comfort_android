package com.example.comfort.services;

import com.example.comfort.api.PcApi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class PcService {
    private static PcService mInstance;
    private static final String HOME_IP = "192.168.31.52";
    private static final byte[] HOME_MAC = getMacBytes("04:d9:f5:36:17:60");
    private static final String BASE_URL = "http://" + HOME_IP;
    private Retrofit mRetrofit;

    private PcService() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    public static PcService getInstance() {
        if (mInstance == null) {
            mInstance = new PcService();
        }
        return mInstance;
    }

    public PcApi getApi() {
        return mRetrofit.create(PcApi.class);
    }

    public void wakeUp() {
        try {
            byte[] bytes = new byte[6 + 16 * HOME_MAC.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += HOME_MAC.length) {
                System.arraycopy(HOME_MAC, 0, bytes, i, HOME_MAC.length);
            }

            InetAddress address = InetAddress.getByName(HOME_IP);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}
