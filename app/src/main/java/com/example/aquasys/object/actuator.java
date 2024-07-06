package com.example.aquasys.object;

import android.graphics.drawable.Drawable;

import com.example.aquasys.R;

import java.util.ArrayList;
import java.util.List;


public class actuator {
    String name;
    int status;
    int img;
    int hour;
    int minute;

    boolean flag;

    boolean is_schedule;
    public enum typeof_actuator{
        bulb,
        pump,
        heater,
        feeder
    }
    typeof_actuator type;
    String id;


    //no-argument constructor
    public actuator(){}
    // constructor
    public actuator(String name, int img   , int status , typeof_actuator type , String id , boolean flag , boolean is_schedule , int hour ,int minute) {
        this.name = name;
        this.img = img;
        this.status = status;
        this.type = type;
        this.id = id;
        this.flag = flag;
        this.is_schedule = is_schedule;
        this.hour = hour;
        this.minute = minute ;
    }

    public boolean isIs_schedule() {
        return is_schedule;
    }

    public void setIs_schedule(boolean is_schedule) {
        this.is_schedule = is_schedule;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public typeof_actuator getType() {
        return type;
    }

    public void setType(typeof_actuator type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public static List<actuator> globalActuator_environment = new ArrayList<>();
    public static List<actuator> globalActuator_water = new ArrayList<>();

    public static List<actuator> globalActuator_environment_add = new ArrayList<>();
    public static List<actuator> globalActuator_water_add = new ArrayList<>();

    public static List<actuator> globalActuator_timer = new ArrayList<>();

    public static List<actuator> globalActuator_add = new ArrayList<>();
    public static List<actuator> globalActuator_edit = new ArrayList<>();
    // actuator environment
    public static List<actuator> listActuator_environment(){
        if(globalActuator_environment.isEmpty()){
           globalActuator_environment.add(0, new actuator("BULB 1" , R.drawable.lightbulb , 0 ,typeof_actuator.bulb ,"ENA000001", false, false,0 , 5));
        }
        return globalActuator_environment;
    }
    // actuator water
    public static List<actuator> listActuator_water(){
        if(globalActuator_water.isEmpty()){
            globalActuator_water.add(0, new actuator("PUMP 1" , R.drawable.water_pump , 0,typeof_actuator.pump , "WTA00001" , false, false,0 , 5));
            globalActuator_water.add(1, new actuator("HEATER 1" , R.drawable.heater , 0 ,typeof_actuator.heater , "WTA00002" , false, false,0 , 5));
            globalActuator_water.add(2, new actuator("FEEDER 1" , R.drawable.fish_feeder , 0 ,typeof_actuator.feeder ,"WTA000003",  false, false,0 , 5));
        }
        return globalActuator_water;
    }
    // actuator environment
    public static List<actuator> listActuator_environment_add(){
        if(globalActuator_environment_add.isEmpty()){
            globalActuator_environment_add.add(0, new actuator("BULB 2" , R.drawable.lightbulb , 0 ,typeof_actuator.bulb ,"", false , false,0 , 5));
        }
        return globalActuator_environment_add;
    }
    // actuator water
    public static List<actuator> listActuator_water_add(){
        if(globalActuator_water_add.isEmpty()){
            globalActuator_water_add.add(0, new actuator("PUMP 1" , R.drawable.water_pump , 0,typeof_actuator.pump ,"", false , false ,0 , 5));
            globalActuator_water_add.add(1, new actuator("HEATER 1" , R.drawable.heater , 0 ,typeof_actuator.heater,"" , false , false,0 , 5));
            globalActuator_water_add.add(2, new actuator("FEEDER 1" , R.drawable.fish_feeder , 0 ,typeof_actuator.feeder,"" , false ,false,0 , 5));
        }
        return globalActuator_water_add;
    }


}
