package com.example.faceappdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private final int ALL_PERMISSIONS_REQUEST_CODE = 100;
    private final String[] permissions = new String[]{Manifest.permission.INTERNET
    ,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isPermissionsGranted = true;
        for (String permission : permissions){
            if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                isPermissionsGranted = false;
                break;
            }
        }
        if (isPermissionsGranted){
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, DetectActivity.class);
                    startActivity(intent);
                }
            }, 3000);
        }else{
            ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_REQUEST_CODE){
            boolean isPermissionsGranted = true;
            for (int result : grantResults){
                if (result != PackageManager.PERMISSION_GRANTED){
                    isPermissionsGranted = false;
                    break;
                }
            }
            if (isPermissionsGranted){
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, DetectActivity.class);
                        startActivity(intent);
                    }
                }, 3000);
            }else{
               finish();
            }
        }
    }
}