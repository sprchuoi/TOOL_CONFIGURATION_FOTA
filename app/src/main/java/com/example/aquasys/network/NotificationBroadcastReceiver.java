package com.example.aquasys.network;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.widget.Toast;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private  final  String TAG = "Wakeup Screen";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        // net work receiver check Connection with Wakeup wifi
        try {
            if (isOnline(context)) {
                Toast.makeText(context, "Network Connected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT).show();
                Intent suspendedScreenIntent = new Intent(context, NoInternetActivity.class);
                suspendedScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(suspendedScreenIntent);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        // network Power Manager
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn() ;
        if(!isScreenOn){
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }
        // ID notification
        int notificationId = intent.getIntExtra("notificationID", 0);
        //cancel notification optional
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
        
    }

    public boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }

    }
}
