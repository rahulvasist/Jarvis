package com.example.rahul.jarvis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiChangeReceiver extends BroadcastReceiver {
    private String  HomeSSID = "CrimeMasterGogo";

    public WifiChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("asdf", "******************** Received my intent ***************");
        if (intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE) != SupplicantState.COMPLETED) {
            return;
        }

        // Connection has been established. Check if we have IP address
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        while(wifiManager.getConnectionInfo().getIpAddress() == 0) {
            Log.d("asdf", "Do not have Ip address yet");
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        Log.d("asdf", "Finally got an IP address");
        new SendCommandToServer().execute("192.168.0.20", "E");
    }
}
