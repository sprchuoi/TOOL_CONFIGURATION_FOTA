package com.example.aquasys.object;

import com.example.aquasys.R;

import java.util.ArrayList;
import java.util.List;

public class nodeSensor {
    private  String addressSensor;
    private List<sensor> listSensor;
    private int applVersion_main;
    private String status_update;
    private int applVersion_sub;
    private String time_Update;
    private String status_Node;
    private String time_OTA;
    private String OTA_Duration;

    // No-argument constructor
    public nodeSensor() {}

    public nodeSensor(String addressSensor, List<sensor> listSensor, int applVersion_main , int applVersion_sub , String status_update, String time_Update ,String status_Node , String time_OTA , String OTA_Duration) {
        this.addressSensor = addressSensor;
        this.listSensor = listSensor;
        this.applVersion_main = applVersion_main;
        this.applVersion_sub = applVersion_sub ;
        this.status_update = status_update ;
        this.time_Update = time_Update;
        this.status_Node  =status_Node;
        this.time_OTA = time_OTA;
        this.OTA_Duration = OTA_Duration;
    }

    public String getTime_OTA() {
        return time_OTA;
    }

    public void setTime_OTA(String time_OTA) {
        this.time_OTA = time_OTA;
    }

    public String getOTA_Duration() {
        return OTA_Duration;
    }

    public void setOTA_Duration(String OTA_Duration) {
        this.OTA_Duration = OTA_Duration;
    }

    public String getStatus_Node() {
        return status_Node;
    }

    public void setStatus_Node(String status_Node) {
        this.status_Node = status_Node;
    }

    public String getTime_Update() {
        return time_Update;
    }

    public void setTime_Update(String time_Update) {
        this.time_Update = time_Update;
    }

    public int getApplVersion_main() {
        return applVersion_main;
    }

    public void setApplVersion_main(int applVersion_main) {
        this.applVersion_main = applVersion_main;
    }

    public String getStatus_update() {
        return status_update;
    }

    public int getApplVersion_sub() {
        return applVersion_sub;
    }

    public void setApplVersion_sub(int applVersion_sub) {
        this.applVersion_sub = applVersion_sub;
    }
//
//    public String isStatus_update() {
//        return status_update;
//    }

    public void setStatus_update(String status_update) {
        this.status_update = status_update;
    }

    public String getAddressSensor() {
        return addressSensor;
    }

    public void setAddressSensor(String addressSensor) {
        this.addressSensor = addressSensor;
    }

    public List<sensor> getListSensor() {
        return listSensor;
    }

    public void setListSensor(List<sensor> listSensor) {
        this.listSensor = listSensor;
    }
    private static final List<nodeSensor> globalListNodeSensor = new ArrayList<>();

    public static List<nodeSensor> getGlobalListNodeSensor() {
        return globalListNodeSensor;
    }

    public static List<nodeSensor> listNode() {
        if (globalListNodeSensor.isEmpty()) {
            globalListNodeSensor.add(new nodeSensor("26011BCD", sensor.listSensor_environment() , 1 , 1,"False","Nah" , "off","Nah" ,"Nah"));
            globalListNodeSensor.add(new nodeSensor("26011DEF", sensor.listSensor_environment() , 1,1, "False" , "Nah", "off", "Nah" ,"Nah"));
            globalListNodeSensor.add(new nodeSensor("260120F0", sensor.listSensor_environment() , 1,1,"False" , "Nah", "off", "Nah" ,"Nah"));
        }
        return globalListNodeSensor;
    }
}
