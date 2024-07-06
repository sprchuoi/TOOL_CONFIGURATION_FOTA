package com.example.aquasys.object;

import com.example.aquasys.R;

import java.util.ArrayList;
import java.util.List;

public class sensor {
    String name;
    String value;
    int img;
    int status;
    typeofsensor type;
    String id;

    public String getType() {
        return type.toString();
    }

    public void setType(typeofsensor type) {
        this.type = type;
    }

    enum typeofsensor{
        humidity,
        temp,
        gas,
        light,
        CO,
        CO2
    }
    //no-argument constructor
    public sensor(){}

    public sensor(String name,String value, int img , typeofsensor type) {
        this.name = name;
        this.value = value;
        this.img = img;
        this.type = type;
    }



    public static List<sensor> getGlobalSensor_enviroment() {
        return globalSensor_enviroment;
    }

    public static void setGlobalSensor_enviroment(List<sensor> globalSensor_enviroment) {
        sensor.globalSensor_enviroment = globalSensor_enviroment;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public static  List<sensor> globalSensor_enviroment = new ArrayList<>();
    public int getImageResource(String sensorName) {
        switch (sensorName) {
            case "Humidity":
                return R.drawable.humidity_percentage;
            case "CO2":
                return R.drawable.co2;
            case "Temperature":
                return R.drawable.device_thermostat;
            case "Water level":
                return R.drawable.water_level;
            case "Moisture Soil":
                return R.drawable.soil;
            case "Co2":
                return R.drawable.co2;
            case "Spo2":
                return R.drawable.spo2;
            case "Light":
                return R.drawable.light_mode;
            default:
                return R.drawable.sensors;
        }
    }
    // sensor list for aqua
    public static List<sensor> listSensor_environment() {
        if (globalSensor_enviroment.isEmpty()) {
            globalSensor_enviroment.add(new sensor("Humidity", "-1", R.drawable.humidity_percentage, typeofsensor.humidity));
            globalSensor_enviroment.add(new sensor("Temperature", "-1", R.drawable.device_thermostat, typeofsensor.temp));
            globalSensor_enviroment.add(new sensor("Light Sensor", "-1", R.drawable.light_mode, typeofsensor.light));
            globalSensor_enviroment.add(new sensor("Gas Sensor", "-1", R.drawable.soil, typeofsensor.gas));
        }
        return globalSensor_enviroment;
    }

}

