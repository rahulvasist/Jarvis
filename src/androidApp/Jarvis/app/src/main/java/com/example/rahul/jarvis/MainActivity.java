package com.example.rahul.jarvis;

import android.app.VoiceInteractor;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

class SendCommandToServer extends AsyncTask<String, Void, String> {
    private int     ESP_PORT = 5000;

    protected String doInBackground(String... params) {
        String  serverReply;
        int     MAX_RETRIES = 5;
        String  ipAddress = params[0];
        String  command = params[1];

        for (int tries = 0; tries < MAX_RETRIES; tries++) {
            try {
                Socket clientSocket = new Socket(ipAddress, ESP_PORT);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                outToServer.writeBytes(command + '\n');
                serverReply = inFromServer.readLine();
                clientSocket.close();
                return (serverReply);

            } catch (Exception e) {
                Log.d("In send command", "Exception thrown: " + e);
            }
        }

        return ("NACK");
    }

    protected void onPostExecute(String result) {
        Log.d("asdf", "Reply: " + result);
    }
}

public class MainActivity extends AppCompatActivity {
    private EditText ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddress = (EditText) findViewById(R.id.ipAddressTextField);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID", wifiInfo.getSSID());
        Log.d("IP: ", String.valueOf(wifiInfo.getIpAddress()));
    }

    public void onButtonClicked(View view) {
        new SendCommandToServer().execute(ipAddress.getText().toString(), "E");
    }

    public void offButtonClicked(View view) {
        new SendCommandToServer().execute(ipAddress.getText().toString(), "D");
    }
}
