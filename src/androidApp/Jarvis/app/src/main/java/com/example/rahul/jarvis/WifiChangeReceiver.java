package com.example.rahul.jarvis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class WifiChangeReceiver extends BroadcastReceiver {
    private String  HomeSSID = "\"CrimeMasterGogo\"";

    public WifiChangeReceiver() {
    }

    private boolean sunAlreadySet(Context context) {
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            for (int i = 0; i < providers.size(); i++) {
                location = locationManager.getLastKnownLocation(providers.get(i));
                if (location != null)
                    break;
            }

            if (location == null) {
                Log.d("asdf", "Cannot find location");
                return (false);
            }

            com.luckycatlabs.sunrisesunset.dto.Location loc = new com.luckycatlabs.sunrisesunset.dto.Location(
                    String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude())
            );
            SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(loc, TimeZone.getDefault());
            Calendar sunsetTime = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance());
            Calendar currentTime = Calendar.getInstance();

            if (currentTime.compareTo(sunsetTime) > 0) {
                return (true);
            } else {
                return (false);
            }

        } catch (SecurityException e) {
            Log.d("asdf", "Exception: " + e);
        }

        return (false);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("asdf", "******************** Received my intent ***************");

        if (!sunAlreadySet(context)) {
            Log.d("asdf", "Sun is still UP!!!");
            return;
        }

        if (intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE) != SupplicantState.COMPLETED) {
            Log.d("asdf", "Wifi not completely up");
            return;
        }

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.getConnectionInfo().getSSID().equals(HomeSSID)) {
            Log.d("asdf", "Not the correct SSID");
            return;
        }

        while(wifiManager.getConnectionInfo().getIpAddress() == 0) {
            Log.d("asdf", "Do not have Ip address yet");
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        Log.d("asdf", "Finally got an IP address");

        /* Send enable command to the switch */
        Intent msgIntent = new Intent(context, CommunicationService.class);
        msgIntent.putExtra("command", "E");
        context.startService(msgIntent);
    }
}
