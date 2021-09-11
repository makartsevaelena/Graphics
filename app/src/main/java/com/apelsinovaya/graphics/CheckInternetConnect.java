package com.apelsinovaya.graphics;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CheckInternetConnect implements Runnable {
    InetAddress address;

    @Override
    public void run() {
        try {
            address = InetAddress.getByName("www.google.com");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    boolean isInternetAvailable() {
        return !address.equals("");
    }
}