package com.example.aquasys.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.aquasys.MainActivity;
import com.example.aquasys.MyApplication;
import com.example.aquasys.R;
import com.example.aquasys.adapter.NodeSensorAdapter;
import com.example.aquasys.adapter.SensorAdapter;
import com.example.aquasys.listener.SelectListener;
import com.example.aquasys.network.NotificationBroadcastReceiver;
import com.example.aquasys.object.nodeSensor;
import com.example.aquasys.object.sensor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;

public class SensorFragment extends Fragment {

    private static final String CHANEL_1_ID = "666";
    private static final String CHANEL_MAIN = "111";
    private static final String CHANEL_2_ID = "2";
    private List<nodeSensor> nodeSensorList;;
    private List<nodeSensor> nodeSensorList_temp;
    private RecyclerView recyclerview_sensor_environment; // recyclerView for sensor
    NodeSensorAdapter nodeSensorAdapter;
    MainActivity mMainActivity;
    RecyclerView recyclerviewNodeSensor;

    private Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, Long> lastUpdateMap = new HashMap<>();
    //    private RecyclerView recyclerview_sensor_water; // recyclerView for sensor

    //private SensorAdapter sensorAdapter_environment, sensorAdapter_water; // adapter for the sensor fragment
    //private LocalDateTime last_time , now ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //override On resume method when changing the fragment
    @Override
    public void onResume() {
        super.onResume();
        //Load sensor to firebase
        //sensorAdapter.notifyDataSetChanged(); // re-change data base on resume method

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        // A reference to the main view of the fragment.
        View mView = inflater.inflate(R.layout.fragment_sensor, container, false); // Inflate the fragment_home layout into mView.
        // A reference to the main activity.
        mMainActivity = (MainActivity) getActivity(); // Get a reference to the hosting Activity (assumed to be MainActivity).
        //recyclerview_sensor_environment = mView.findViewById(R.id.recyclerview_sensor_list); // Find the RecyclerView in the layout.
        // recycleView for sensor Node
        recyclerviewNodeSensor = mView.findViewById((R.id.recyclerview_node_sensor));
        nodeSensorList_temp = new ArrayList<>();
        nodeSensorList = nodeSensor.listNode();
//        recyclerview_sensor_water = mView.findViewById(R.id.recyclerview_sensor_water); // Find the RecyclerView in the layout.
        // setting show sensor list in the recyclerview
        fetchNodeSensorsFromFirebase();
        GridLayoutManager gridLayoutManager_SensorNode = new GridLayoutManager(mMainActivity,1);
        //GridLayoutManager gridLayoutManager_environment = new GridLayoutManager(mMainActivity, 2);
        nodeSensorAdapter =new NodeSensorAdapter(getContext() ,nodeSensorList);
        //set layout
        recyclerviewNodeSensor.setLayoutManager(gridLayoutManager_SensorNode);
//        GridLayoutManager gridLayoutManager_water = new GridLayoutManager(mMainActivity, 2);
        // setting show environment sensor
        //recyclerview_sensor_environment.setLayoutManager(gridLayoutManager_environment);]
        recyclerviewNodeSensor.setAdapter(nodeSensorAdapter);
        nodeSensorAdapter.notifyDataSetChanged();

        // setting show water sensor
//        recyclerview_sensor_water.setLayoutManager(gridLayoutManager_water);
        //get the start time
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            last_time  = LocalDateTime.now();
//        }
//        sensorAdapter_environment = new SensorAdapter(sensor.listSensor_environment(), new SelectListener() {
//            @Override
//            public void onClickItemSensor(sensor sen, int position) {
//                mMainActivity.goToSensorDevice(sen.getName(), position, sen.getType());
//
//            }
//        });
//        sensorAdapter_water = new SensorAdapter(sensor.listSensor_water(), new SelectListener() {
//            @Override
//            public void onClickItemSensor(sensor sen, int position) {
//                mMainActivity.goToSensorDevice(sen.getName(), position, sen.getType());
//            }
//        });        //Set Sensor adapter
        //recyclerview_sensor_environment.setAdapter(sensorAdapter_environment);
        //recyclerview_sensor_water.setAdapter(sensorAdapter_water);
        // Realtime database reading sensor check environment
        // Call the method for each sensor
        if (nodeSensorList != null && !nodeSensorList.isEmpty()) {
            for (int i = 0; i < nodeSensorList.size(); i++) {
                updateSensorData_environment(i, "value1", 0);
                updateSensorData_environment(i, "value2", 1);
                updateSensorData_environment(i, "value3", 2);
                updateSensorData_environment(i, "value4", 3);
                // Update the sensorNode
                updateTime_getData(i);
            }
        }
        // notification of sensor value
        //mMainActivity.Notification();
      return mView;
     }


    private void fetchNodeSensorsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("SensorNode");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //nodeSensorList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    nodeSensor nodeSensor = snapshot.getValue(nodeSensor.class);
                    if (nodeSensor != null) {
                        nodeSensorList_temp.add(nodeSensor);
                    }
                }
                // Ensure not return null
                if(nodeSensorList_temp.size()>=3){
                    nodeSensorList= nodeSensorList_temp;
                }

                nodeSensorAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // read data form firebase
    // Read data from Firebase for a specific sensor and update the corresponding sensor object

    // Read data sensor from environment
    // Define a common method to update sensor data
    private void updateSensorData_environment(int nodeSensorIndex,String sensorKey, int sensorIndex) {
        mMainActivity.mDatabaseSensor_Data.child(nodeSensorList.get(nodeSensorIndex).getAddressSensor()).child(sensorKey).addValueEventListener(new ValueEventListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the value as an Object to handle different possible data types
                Object valueObj = snapshot.getValue();
                String value = null;

                // Convert the value to String if it's a valid type
                if (valueObj instanceof String) {
                    value = (String) valueObj;
                } else if (valueObj instanceof Long) {
                    value = String.valueOf(valueObj);
                }

                // Check if the value is not null and is a valid decimal number
                if (value != null && isValidDecimalNumber(value)) {
                    nodeSensorList.get(nodeSensorIndex).getListSensor().get(sensorIndex).setValue(value);
                    nodeSensorAdapter = new NodeSensorAdapter(getContext(), nodeSensorList);
                    recyclerviewNodeSensor.setAdapter(nodeSensorAdapter);
                    //nodeSensorAdapter.notifyDataSetChanged();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mMainActivity, "Error occurred while reading data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // get the realtime Update data on firebase
    private void updateTime_getData(int nodeSensorIndex){
        mMainActivity.mDatabaseSensor_Data.child(nodeSensorList.get(nodeSensorIndex).getAddressSensor()).child("TimeUpdate").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the value as an Object to handle different possible data types
                Object valueObj = snapshot.getValue();
                String value = null;

                // Convert the value to String if it's a valid type
                if (valueObj instanceof String) {
                    value = (String) valueObj;
                } else if (valueObj instanceof Long) {
                    value = String.valueOf(valueObj);
                }
                if(value !=null && isValidDecimalNumber((value))){
                    // format timedate
                    String formattedDate = formatFirebaseDateTime(value);
                    if(formattedDate != null){
                        nodeSensorList.get(nodeSensorIndex).setTime_Update(value);
                        nodeSensorList.get(nodeSensorIndex).setStatus_Node("on");
                        //nodeSensorAdapter = new NodeSensorAdapter(getContext(), nodeSensorList);
                        //recyclerviewNodeSensor.setAdapter(nodeSensorAdapter);
                        // Cập nhật thời gian cập nhật lần cuối của thiết bị
                        long firebaseTimeMillis = convertFirebaseTimeToMillis(value);
                        lastUpdateMap.put(nodeSensorList.get(nodeSensorIndex).getAddressSensor(), firebaseTimeMillis);
                        nodeSensorAdapter.notifyDataSetChanged();
                    }
                    else  Toast.makeText(mMainActivity, "Format Read Error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mMainActivity, "Error occurred while reading data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // format firebase time
    private String formatFirebaseDateTime(String firebaseDateTime) {
        SimpleDateFormat firebaseDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        try {
            Date date = firebaseDateFormat.parse(firebaseDateTime);
            if(date !=null)
                return displayDateFormat.format(date);
            else
                // error
                return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // hoặc trả về một chuỗi mặc định nào đó
        }
    }
    // Convert Firebase time to milliseconds
    private long convertFirebaseTimeToMillis(String firebaseDateTime) {
        SimpleDateFormat firebaseDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        try {
            Date date = firebaseDateFormat.parse(firebaseDateTime);
            if (date != null) {
                //Toast.makeText(getContext(), "Time firebase :" + date.getTime(), Toast.LENGTH_SHORT).show();
                return date.getTime();
            }
            else return 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    // Check decimal number
    // Utility method to check if a String is a valid decimal number
    private boolean isValidDecimalNumber(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private void checkDeviceStatusPeriodically() {
        handler = new Handler();
        Runnable updateRunnable = new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                long firebaseTimeMillis = 0;
                for (int i = 0; i < nodeSensorList.size(); i++) {
                    String firebaseTime = nodeSensorList.get(i).getTime_Update();
                    firebaseTimeMillis = convertFirebaseTimeToMillis(firebaseTime);

                    if ((currentTimeMillis - firebaseTimeMillis) > 30000) { // Nếu quá 30 giây
                        nodeSensorList.get(i).setStatus_Node("off");
                    } else {
                        nodeSensorList.get(i).setStatus_Node("on");
                    }
                    //Toast.makeText(getContext(), "Time firebase :" + firebaseTimeMillis, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(getContext(), "Time firebase :" + firebaseTimeMillis +"\n"
//                            +"Time Sys:"+ currentTimeMillis, Toast.LENGTH_SHORT).show();
                    //

                }
                nodeSensorAdapter.notifyDataSetChanged();
//                Toast.makeText(getContext(), "Time firebase :" + firebaseTimeMillis +"\n"
//                        +"Time Sys:"+ currentTimeMillis, Toast.LENGTH_SHORT).show();
                handler.postDelayed(this, 30000); // 30 giây
            }
        };

        handler.post(updateRunnable);
    }

    // Khởi tạo Handler và Runnable để lấy dữ liệu mỗi 30 giây
//    private Runnable createRunnable(final int nodeSensorIndex) {
//        return new Runnable() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void run() {
//                long currentTime = System.currentTimeMillis();
//                nodeSensor Node_Sensor = nodeSensorList.get(nodeSensorIndex);
//                Long lastUpdateTime = lastUpdateMap.get(Node_Sensor.getAddressSensor());
//                if (lastUpdateTime != null && (currentTime - lastUpdateTime > 30000)) {
//                    Node_Sensor.setStatus_Node("off");
//                } else {
//                    Node_Sensor.setStatus_Node("on");
//                }
//
//                nodeSensorAdapter.notifyDataSetChanged();
//
//                // Gọi hàm lấy dữ liệu từ Firebase
//                updateTime_getData(nodeSensorIndex);
//
//                // Lặp lại runnable sau 30 giây
//                handler.postDelayed(this, 30000);
//            }
//        };
//    }
    // Bắt đầu lấy dữ liệu mỗi 30 giây
    private void startFetchingDataEvery30Seconds() {
        checkDeviceStatusPeriodically();
    }
    // Dừng việc lấy dữ liệu
    private void stopFetchingData() {
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        startFetchingDataEvery30Seconds();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopFetchingData();
    }

    // Read data sensor from environment
//    private void updateSensorData_Water(String sensorKey, int sensorIndex) {
//        mMainActivity.mDatabaseSensor_val.child(sensorKey).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String value = snapshot.getValue(String.class);
//                // check value not null number and the value is the number
//                if (value != null && isValidDecimalNumber(value)) {
//                    sensor.globalSensor_water.get(sensorIndex).setValue(value);
//                    sensorAdapter_water = new SensorAdapter(sensor.globalSensor_water,
//                            (sen, position) -> mMainActivity.goToSensorDevice(sen.getName(), position, sen.getType()));
//                    recyclerview_sensor_water.setAdapter(sensorAdapter_water);
//                    sendCustomNotification();
//                    sensor.globalSensor_water.get(sensorIndex).setStatus(1);
//
//                    if (Float.parseFloat(sensor.listSensor_water().get(1).getValue()) < 10)
//                        sendNotification_water();
//                    else
//                        cancelNotification(Integer.parseInt(CHANEL_1_ID));
//
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                        now = LocalDateTime.now();
//                        if (now.isAfter(last_time)) {
//                            mMainActivity.mDatabaseSensor_water.child(String.valueOf(sensorIndex)).child("status").setValue(sensor.globalSensor_enviroment.get(sensorIndex).getStatus());
//                            last_time = now;
//                        } else sensor.globalSensor_water.get(sensorIndex).setStatus(0);
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(mMainActivity, "Error occurred while reading data", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
    // notification for sensor environment
//    private void sendNotification_environment() {
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_aquaphonics);
//        Intent intent  = new Intent(mMainActivity , MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mMainActivity,0 , intent , PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);
//        Notification notification = new NotificationCompat.Builder(mMainActivity, String.valueOf(MyApplication.CHANNEL_ID_ENVIRONMENT))
//                .setContentTitle("Alarm for activate")
//                .setContentText("Please water your plant")
//                .setSmallIcon(R.drawable.aquaphonic)
//                .setLargeIcon(bitmap)
//                .setColor(getResources().getColor(R.color.blue))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true)
//                .build();
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mMainActivity);
//        if (ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        notificationManager.notify(Integer.parseInt(CHANEL_1_ID), notification);
//
//   }
//    // notification for sensor water
//    private void sendNotification_water(){
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources() , R.mipmap.ic_aquaphonics);
//        Notification notification = new NotificationCompat.Builder(mMainActivity, String.valueOf(MyApplication.CHANNEL_ID_WATER))
//                .setContentTitle("Alarm for activate")
//                .setContentText("Please fill your pool")
//                .setSmallIcon(R.drawable.aquaphonic)
//                .setLargeIcon(bitmap)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setColor(getResources().getColor(R.color.blue))
//                .setAutoCancel(true)
//                .build();
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mMainActivity);
//        if (ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        notificationManager.notify(Integer.parseInt(CHANEL_2_ID), notification);
//
//    }
//    private void sendCustomNotification(){
//        RemoteViews notificationLayout = new RemoteViews(mMainActivity.getPackageName() , R.layout.small_notification_layout);
//        notificationLayout.setTextViewText(R.id.notification_title , "Monitor ");
//        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//        String strDate = sdf.format(new Date());
//        notificationLayout.setTextViewText(R.id.time_notification , strDate);
//        Intent intent  = new Intent(mMainActivity , MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mMainActivity,0 , intent , PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);
//
//        // Notification Expand
//        RemoteViews notificationLayoutExpanded = new RemoteViews(mMainActivity.getPackageName() , R.layout.notification_layout);
//        // mapping information in layout
//        notificationLayoutExpanded.setTextViewText(R.id.tv_humidity_val , sensor.listSensor_environment().get(0).getValue());
//        notificationLayoutExpanded.setTextViewText(R.id.tv_temperature_val , sensor.listSensor_environment().get(1).getValue());
//        notificationLayoutExpanded.setTextViewText(R.id.tv_light_val , sensor.listSensor_environment().get(2).getValue());
//        notificationLayoutExpanded.setTextViewText(R.id.tv_soil_val , sensor.listSensor_environment().get(3).getValue());
//        notificationLayoutExpanded.setTextViewText(R.id.tv_ph_val, sensor.listSensor_water().get(0).getValue());
//        notificationLayoutExpanded.setTextViewText(R.id.tv_waterlevel_val, sensor.listSensor_water().get(1).getValue());
//        Notification notification = new NotificationCompat.Builder(mMainActivity, String.valueOf(MyApplication.CHANNEL_ID))
//                .setSmallIcon(R.drawable.aquaphonic)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setCustomContentView(notificationLayout)
//                .setCustomBigContentView(notificationLayoutExpanded)
//                .setContentIntent(pendingIntent)
//                .setSound(null)
//                .build();
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mMainActivity);
//        if (ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        notificationManager.notify(Integer.parseInt(CHANEL_MAIN), notification);
//        getDeleteIntent();
//    }
//    protected PendingIntent getDeleteIntent() {
//        Intent intent_broadcast = new Intent(mMainActivity,
//                NotificationBroadcastReceiver.class ) ;
//        intent_broadcast.setAction( "notification_cancelled" ) ;
//        return PendingIntent.getBroadcast (mMainActivity, 0 , intent_broadcast , PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE ) ;
//    }
//    // kill notification
//    private void cancelNotification(int notificationId) {
//        NotificationManager notificationManager = mMainActivity.getSystemService(NotificationManager.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            notificationManager.cancel(notificationId);
//        }
//    }


}