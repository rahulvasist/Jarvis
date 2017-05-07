package com.example.rahul.jarvis;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

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
        String  command = intent.getStringExtra("command");

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
                return;

            } catch (Exception e) {
                Log.d("CommunicationService", "Exception thrown: " + e);
            }
        }
    }
}
