package com.example.rahul.jarvis;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private EditText ipAddress;
    private int     MY_PERMISSIONS_REQUEST = 1;

    private void getRuntimePermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //TODO: handle this properly

                } else {

                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddress = (EditText) findViewById(R.id.ipAddressTextField);

        getRuntimePermissions();
    }

    private void sendCommandToServer() {
        Intent intent = new Intent(this, CommunicationService.class);
        intent.putExtra("source", "MainActivity");
        startService(intent);
    }

    private void setLastCommand(String command)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.command_file),
                MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastCommand", command);
        editor.commit();
    }

    public void onButtonClicked(View view) {
        setLastCommand("E");
        sendCommandToServer();
    }

    public void offButtonClicked(View view) {
        setLastCommand("D");
        sendCommandToServer();
    }
}
