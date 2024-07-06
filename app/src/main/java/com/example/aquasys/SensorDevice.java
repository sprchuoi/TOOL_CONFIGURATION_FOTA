package com.example.aquasys;

import static java.sql.Types.NULL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SensorDevice extends AppCompatActivity {
    private String SensorName ;
    private String SensorType ;
    private Toolbar sensorToolbar;
    private Integer sensorStatus;
    private int index;
    private ImageView sensorImgView,sensorImgStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_device);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            SensorName = bundle.getString("nameSensor", "Sensor Name");
            SensorType = bundle.getString("typeSensor", "Sensor Type");
            index = bundle.getInt("index",NULL);
        }
        sensorImgStatus = (ImageView)findViewById(R.id.status_img);
        DataSensor();

        sensorImgView = (ImageView)findViewById(R.id.sensor_img);
        setimgviewSensor();

        sensorToolbar =(Toolbar) findViewById(R.id.sensor_toolbar);
        sensorToolbar.setTitle(SensorName);
        setBackButtonOnToolbar();
    }

    public void setBackButtonOnToolbar() {
        setSupportActionBar(sensorToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(SensorName);
        }

        sensorToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void DataSensor() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (  SensorType.equals("humidity")
            ||SensorType.equals("temp")
            ||SensorType.equals("soil_moisture")
            ||SensorType.equals("light"))
        {
            DatabaseReference ListRef = database.getReference("Sensors_environment");
            DatabaseReference SensorRef = ListRef.child(Integer.toString(index));
            DatabaseReference ImgRef = SensorRef.child("img");

            DatabaseReference statusRef = SensorRef.child("status");
            statusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        sensorStatus = snapshot.getValue(Integer.class);
                        if(sensorStatus == 1)
                        {
                            sensorImgStatus.setImageResource(R.drawable.checked);
                        }
                        else if(sensorStatus == 0)
                        {
                            sensorImgStatus.setImageResource(R.drawable.cancel);
                        }
                        else sensorImgStatus.setImageResource(R.drawable.unknown);

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        if (  SensorType.equals("ph")
                ||SensorType.equals("level_water"))
        {
            DatabaseReference ListRef = database.getReference("Sensors_water");
            DatabaseReference SensorRef = ListRef.child(Integer.toString(index));
            DatabaseReference ImgRef = SensorRef.child("img");

            DatabaseReference statusRef = SensorRef.child("status");
            statusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        sensorStatus = snapshot.getValue(Integer.class);
                        if(sensorStatus == 1)
                        {
                            sensorImgStatus.setImageResource(R.drawable.checked);
                        }
                        else if(sensorStatus == 0)
                        {
                            sensorImgStatus.setImageResource(R.drawable.cancel);
                        }
                        else sensorImgStatus.setImageResource(R.drawable.unknown);

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    public void setimgviewSensor()
    {
        if(SensorType.equals("humidity")) sensorImgView.setImageResource(R.drawable.humidity_mid);
        if(SensorType.equals("temp")) sensorImgView.setImageResource(R.drawable.heater);
        if(SensorType.equals("soil_moisture")) sensorImgView.setImageResource(R.drawable.soil);
        if(SensorType.equals("light")) sensorImgView.setImageResource(R.drawable.light_mode);
        if(SensorType.equals("ph")) sensorImgView.setImageResource(R.drawable.ph);
        if(SensorType.equals("level_water")) sensorImgView.setImageResource(R.drawable.water_level);

    }
}
