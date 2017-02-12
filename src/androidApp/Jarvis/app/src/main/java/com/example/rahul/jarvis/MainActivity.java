package com.example.rahul.jarvis;

import android.app.VoiceInteractor;
import android.content.Context;
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

public class MainActivity extends AppCompatActivity {
    private int     ESP_PORT = 5000;
    private EditText ipAddress;

    private class SendCommandToServer extends AsyncTask<String, Void, String> {
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
            Context context = getApplicationContext();
            CharSequence toastText = "Command Failed";
            Log.d("asdf", "Reply: " + result);

            if (result.equals("ACK")) {
                toastText = "Command succeeded";
            }

            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddress = (EditText) findViewById(R.id.ipAddressTextField);
    }

    public void onButtonClicked(View view) {
        new SendCommandToServer().execute(ipAddress.getText().toString(), "E");
    }

    public void offButtonClicked(View view) {
        new SendCommandToServer().execute(ipAddress.getText().toString(), "D");
    }
}
