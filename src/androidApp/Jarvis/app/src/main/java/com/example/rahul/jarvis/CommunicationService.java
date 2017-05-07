package com.example.rahul.jarvis;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Calendar;

public class CommunicationService extends IntentService {
    public CommunicationService() {
        super("CommunicationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;

        String  serverReply;
        int     MAX_RETRIES = 5;
        String  ipAddress = "192.168.0.20";
        int     ESP_PORT = 5000;
        String  command = getLastCommand();

        for (int tries = 0; tries < MAX_RETRIES; tries++) {
            try {
                Socket clientSocket = new Socket(ipAddress, ESP_PORT);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),
                                "US-ASCII"));

                outToServer.writeBytes(command + '\n');
                serverReply = inFromServer.readLine();
                Log.d("CommunicationService", "Server reply: " + serverReply);
                clientSocket.close();
                setRepeatCommand(command);
                releaseLock(intent);
                return;

            } catch (Exception e) {
                Log.d("CommunicationService", "Exception thrown: " + e);
            }
        }
    }

    private String getLastCommand() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.command_file),
                MODE_PRIVATE);

        return sharedPreferences.getString("lastCommand", "D");
    }

    private void setRepeatCommand(String command) {
        if (command.equals("D")) {
            /* No need to repeat disable commands */
            return;
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        /* Set the next alarm at 5 mins in the future */
        long nextAlarmTime = Calendar.getInstance().getTimeInMillis() + (5 * 60 * 1000);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, nextAlarmTime, pendingIntent);
    }

    private void releaseLock(Intent intent)
    {
        String source = intent.getStringExtra("source");

        if (source == null)
            return;

        if (source.equals("WifiChangeReceiver"))
            WifiChangeReceiver.completeWakefulIntent(intent);
        else if (source.equals("AlarmReceiver"))
            AlarmReceiver.completeWakefulIntent(intent);
    }
}
