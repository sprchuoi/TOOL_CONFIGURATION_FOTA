package com.example.aquasys.object;

import android.os.SystemClock;

import java.util.Calendar;

public class timervariable {
    //get the timer for activate wakeup timer with Pending Intent
    public long RTC_Wakeup_timer ;
    public long getRTCWakeupTimer() {
        // Get the current time
        Calendar currentTime = Calendar.getInstance();

        // Add a specific duration, for example, 30 minutes
        int durationInMinutes = 30;
        currentTime.add(Calendar.MINUTE, durationInMinutes);

        // Get the time in milliseconds
        RTC_Wakeup_timer = currentTime.getTimeInMillis();

        // Convert the result to minus (subtract the current time from the added time)
        RTC_Wakeup_timer = RTC_Wakeup_timer - System.currentTimeMillis();
        return  RTC_Wakeup_timer;
    }
    public long getCurrentTimeInMinutes() {
        // get time in realtime
        long currentTimeInMillis = SystemClock.elapsedRealtime();

        // Convert  current millisecond to minus
        return currentTimeInMillis / 60000;
    }
}
