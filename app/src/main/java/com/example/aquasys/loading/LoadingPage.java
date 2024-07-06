package com.example.aquasys.loading;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.aquasys.MainActivity;
import com.example.aquasys.R;
import com.example.aquasys.network.NotificationBroadcastReceiver;

public class LoadingPage extends AppCompatActivity  {
    // create hander for loadding page
    private Handler handler;
    private MainActivity mMainactivity;

    private NotificationBroadcastReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_page);

        // create loading page
        handler = new Handler();


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(LoadingPage.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }


}