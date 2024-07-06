package com.example.aquasys.network;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.aquasys.MainActivity;
import com.example.aquasys.R;

public class NoInternetActivity extends AppCompatActivity {
    private Handler handler_connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        // mapping for btn
        Button btn_retry_connect = findViewById(R.id.btn_retry_connect);
        ProgressBar progress_bar_connection = findViewById(R.id.progress_bar_connection);
        handler_connection = new Handler();
        progress_bar_connection.setVisibility(View.INVISIBLE);
        btn_retry_connect.setOnClickListener(v -> {
            progress_bar_connection.setVisibility(View.VISIBLE);
            handler_connection.postDelayed(() -> {
                if (InternetIsConnected()){
                    Intent intent = new Intent(NoInternetActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    progress_bar_connection.setVisibility(View.INVISIBLE);
                }
                else {
                    Toast.makeText(NoInternetActivity.this , "No Connection , Try Again !", Toast.LENGTH_SHORT).show();
                    progress_bar_connection.setVisibility(View.INVISIBLE);
                }
            }, 3000);
        });
    }
    public boolean InternetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }
}