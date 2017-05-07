package com.example.rahul.jarvis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent msgIntent = new Intent(context, CommunicationService.class);
        msgIntent.putExtra("source", "AlarmReceiver");
        startWakefulService(context, msgIntent);
    }
}
