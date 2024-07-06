package com.example.aquasys.object;

import java.util.ArrayList;
import java.util.List;

public class timer {
    actuator act;
    int time_start_hour;
    int time_start_minute;
    int  status;
    // Default (no-argument) constructor
    public timer() {
        // You can initialize your fields with default values here if needed
    }

    public timer(actuator act, int time_start_hour, int time_start_minute , int  status) {
        this.act = act;
        this.time_start_hour = time_start_hour;
        this.time_start_minute = time_start_minute;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static List<timer> getGlobalTimer() {
        return globalTimer;
    }

    public static void setGlobalTimer(List<timer> globalTimer) {
        timer.globalTimer = globalTimer;
    }

    public actuator getAct() {
        return act;
    }

    public void setAct(actuator act) {
        this.act = act;
    }

    public int getTime_start_hour() {
        return time_start_hour;
    }

    public void setTime_start_hour(int time_start_hour) {
        this.time_start_hour = time_start_hour;
    }

    public int getTime_start_minute() {
        return time_start_minute;
    }

    public void setTime_start_minute(int time_start_minute) {
        this.time_start_minute = time_start_minute;
    }

    // global timer schedule for automation active activate
    public static List<timer> globalTimer = new ArrayList<>();

    public boolean is_update = false;
    public int pos_delete = 0;
}
