//package com.example.aquasys.fragment;
//
//import static android.app.Notification.EXTRA_NOTIFICATION_ID;
//
//import android.annotation.SuppressLint;
//import android.app.AlarmManager;
//import android.app.Dialog;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.os.Build;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.core.app.NotificationCompat;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.DividerItemDecoration;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.os.SystemClock;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.widget.NumberPicker;
//import android.widget.RemoteViews;
//import android.widget.Toast;
//import android.widget.ToggleButton;
//
//import com.example.aquasys.MainActivity;
//import com.example.aquasys.MyApplication;
//import com.example.aquasys.R;
//import com.example.aquasys.adapter.TimerActuatorAdapter;
//import com.example.aquasys.adapter.TimerAdapter;
//import com.example.aquasys.object.actuator;
//import com.example.aquasys.object.timer;
//import com.example.aquasys.object.timervariable;
//import com.example.aquasys.system.SharedPreferencesHelper;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.ValueEventListener;
//import com.google.gson.reflect.TypeToken;
//
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.Duration;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.temporal.ChronoField;
//import java.util.Calendar;
//import java.util.Date;
//
//import java.util.List;
//import java.util.Objects;
//
//public class TimerFragment extends Fragment {
//
//    private static final int NOTIFICATION_ID = 1;
//    private NumberPicker np_duration_hour_from , np_duration_minute_from , np_duration_am_pm;
//    private MainActivity mMainActivity;
//    private RecyclerView recyclerview_timer;
//    private TimerAdapter timerAdapter;
//    private  String selectedActuatorName;
//    private long timeDifferential ;
//    private long timeCurrent_min ;
//    private timervariable timerVariable;
//    private static final String ACTION_NOTIFY =
//            "com.example.android.timer.ACTION_NOTIFY";
//
//    public TimerFragment() {
//    }
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//    @SuppressLint("NotifyDataSetChanged")
//    @Override
//    public void onResume() {
//        super.onResume();
//        timerAdapter.notifyDataSetChanged(); // re-change data base on resume method
//
//    }
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//
//
//        // Inflate the layout for this fragment
//        View mView = inflater.inflate(R.layout.fragment_timer, container, false);
//        //GetContext for main Activity
//        mMainActivity = (MainActivity) getContext();
//        FloatingActionButton btn_timer = mView.findViewById(R.id.btn_timer);
//        btn_timer.setOnClickListener(v -> showBottomDiaLog());
//        // Define a boolean variable to keep track of the currently selected button
//        // Inflate the layout for this fragment.
//        recyclerview_timer = mView.findViewById(R.id.recyclerview_timer); // Find the RecyclerView in the layout.
//        // setting show sensor list in the recyclerview
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mMainActivity);
//        recyclerview_timer.setLayoutManager(linearLayoutManager);
//        // set list for sensor adapter
//        timerAdapter = new TimerAdapter(timer.globalTimer, (tim, position) -> {
//        });
//        recyclerview_timer.setAdapter(timerAdapter);
//        recyclerview_timer.addItemDecoration(new DividerItemDecoration(recyclerview_timer.getContext(), DividerItemDecoration.VERTICAL));
//        //read data from schedule firebase
//        List<timer> retrievedTimerList = SharedPreferencesHelper.getListFromSharedPreferences(mMainActivity, "schedule", new TypeToken<List<timer>>() {});
//        if(retrievedTimerList == null)
//            ReadScheduleData();
//        return mView;
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private void showBottomDiaLog(){
//        final Dialog dialog = new Dialog(requireContext());
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        /// get View from layout timer
//        @SuppressLint("InflateParams") View mViewDialog = mMainActivity.getLayoutInflater().inflate(R.layout.dialog_set_timer,null);
//        dialog.setContentView(mViewDialog);
//        dialog.show();
//        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
//        dialog.getWindow().setGravity(Gravity.BOTTOM);
//        mMainActivity.tmp_actuator = null;
//        //processing imf in bottom sheet
//        // Set a recyclerview
//        RecyclerView recyclerview_adapter_timer_water = mViewDialog.findViewById(R.id.recyclerview_adapter_timer_water); // Find the RecyclerView in the layout.
//        RecyclerView recyclerview_adapter_timer_environment = mViewDialog.findViewById(R.id.recyclerview_adapter_timer_environment); // Find the RecyclerView in the layout.
//        // set GridlayoutManager
//        GridLayoutManager gridLayoutManager_water = new GridLayoutManager(mMainActivity , 2);
//        GridLayoutManager gridLayoutManager_environment = new GridLayoutManager(mMainActivity , 2);
//        // set gridLayoutManager for recyclerview
//        recyclerview_adapter_timer_water.setLayoutManager(gridLayoutManager_water);
//        recyclerview_adapter_timer_environment.setLayoutManager(gridLayoutManager_environment);
//        // set data for actuator in timer
//        // Actuator adapter for fish
//        TimerActuatorAdapter timerActuatorAdapter_water = new TimerActuatorAdapter(actuator.globalActuator_water);
//        // Actuator adapter for tree
//        TimerActuatorAdapter timerActuatorAdapter_environment = new TimerActuatorAdapter(actuator.globalActuator_environment);
//
//        // set recyclerview with adapter
//        recyclerview_adapter_timer_environment.setAdapter(timerActuatorAdapter_environment);
//        recyclerview_adapter_timer_water.setAdapter(timerActuatorAdapter_water);
//
//        // setting mapping components in Dialog set timer
//        FloatingActionButton btn_done = mViewDialog.findViewById(R.id.btn_done);
//        FloatingActionButton btn_close = mViewDialog.findViewById(R.id.btn_close);
//        np_duration_hour_from = mViewDialog.findViewById(R.id.np_duration_hour_from);
//        np_duration_minute_from = mViewDialog.findViewById(R.id.np_duration_minute_from);
//        np_duration_am_pm = mViewDialog.findViewById(R.id.np_duration_am_pm);
//        // set max && min for np
//        np_duration_hour_from.setMaxValue(12);
//        np_duration_hour_from.setMinValue(0);
//        np_duration_minute_from.setMaxValue(59);
//        np_duration_minute_from.setMinValue(0);
//        //set AM and PM for time picker
//
//        String[] displayedValues = {"AM", "PM"};
//        np_duration_am_pm.setMinValue(0);
//        np_duration_am_pm.setMaxValue(displayedValues.length - 1);
//        np_duration_am_pm.setDisplayedValues(displayedValues);
//
//        //Button select when choosing device for schedule timer
//        // get list actuator for tree
//        btn_done.setOnClickListener(v -> {
//
//            if (actuator.globalActuator_timer != null) {
//                int hourFrom = 0;
//                if (np_duration_am_pm.getValue() == 0) {
//                    hourFrom = np_duration_hour_from.getValue();
//                } else hourFrom = np_duration_hour_from.getValue() + 12;
//                int minuteFrom = np_duration_minute_from.getValue();
//
//                for (int i = 0; i < actuator.globalActuator_timer.size(); i++) {
//                    // Add the timer
//                    if(actuator.globalActuator_timer.get(i).getMinute() == 0 &&actuator.globalActuator_timer.get(i).getHour() == 0  ) {
//                        Toast.makeText(mMainActivity, "Please select time for schedule ! ", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if(actuator.globalActuator_timer.get(i).isIs_schedule()){
//                        Toast.makeText(mMainActivity, "Actuator is scheduled !!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    // Set flag is_schedule for actuator timer to true
//                    // check the type of actuator in global actuator timer is used for environment
//                    if(actuator.globalActuator_timer.get(i).getType() == actuator.typeof_actuator.bulb){
//                        for(int j = 0 ; j < actuator.globalActuator_environment.size() ; j++){
//                            if(actuator.globalActuator_environment.get(j).getId().equals(actuator.globalActuator_timer.get(i).getId())) {
//                                actuator.globalActuator_environment.get(j).setIs_schedule(true);
//                                mMainActivity.mDatabaseActuator_environment.child(String.valueOf(j)).child("is_schedule").setValue(true);
//                            }
//                        }
//                    }
//                    else {
//                        for(int j = 0 ; j < actuator.globalActuator_water.size() ; j++){
//                            if(actuator.globalActuator_water.get(j).getId().equals(actuator.globalActuator_timer.get(i).getId())) {
//                                actuator.globalActuator_water.get(j).setIs_schedule(true);
//                                mMainActivity.mDatabaseActuator_water.child(String.valueOf(j)).child("is_schedule").setValue(true);
//                            }
//                        }
//                    }
//                    timer.globalTimer.add(new timer(actuator.globalActuator_timer.get(i), hourFrom, minuteFrom, 1));
//
//
//                }
//
//                // find the nearest time with the the actuator and show notification
//                int nearestActuator = findNearestActuator(actuator.globalActuator_timer);
//                int startHour = timer.globalTimer.get(nearestActuator).getTime_start_hour();
//                int startMinute = timer.globalTimer.get(nearestActuator).getTime_start_minute();
//                String amPm;
//
//                if (startHour <= 12) {
//                    amPm = "AM";
//                    if (startHour == 0) {
//                        startHour = 12; // Convert 0 AM to 12 AM
//                    }
//                } else {
//                    amPm = "PM";
//                    startHour -= 12; // Convert 24-hour format to 12-hour format
//                }
//                @SuppressLint("DefaultLocale") String formattedTimeRange = String.format("%02d:%02d %s", startHour, startMinute, amPm);
//                selectedActuatorName = timer.globalTimer.get(nearestActuator).getAct().getName();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    sendNotification(("Timer Set for " + selectedActuatorName), "Status : ON", formattedTimeRange);
//                }
//                timerAdapter.notifyDataSetChanged();
//                recyclerview_timer.setAdapter(timerAdapter);
//                mMainActivity.addScheduleToFireBase();
//                SharedPreferencesHelper.saveListToSharedPreferences(mMainActivity, timer.globalTimer , "schedule");
//                dialog.cancel();
//                // make clear global actuator
//                actuator.globalActuator_timer = null;
//            }
//
//            else {
//                // Display a toast when temp_act is null
//                Toast.makeText(getContext(), "Please Select Actuator !!", Toast.LENGTH_SHORT).show();
//            }
//
//        });
//
//        btn_close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.cancel();
//            }
//        });
//    }
//    public boolean is_Read = false;
//    // Read Scheduler data from firebase only once
//    private void ReadScheduleData() {
//        if (!is_Read) {
//            mMainActivity.mDatabaseSchedule.addListenerForSingleValueEvent(new ValueEventListener() {
//                @SuppressLint("NotifyDataSetChanged")
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        timer schedule = dataSnapshot.getValue(timer.class);
//                        timer.globalTimer.add(schedule);
//                        Toast.makeText(mMainActivity, "Reading success", Toast.LENGTH_SHORT).show();
//                    }
//                    // Notify the adapter that the data set has changed
//                    timerAdapter.notifyDataSetChanged();
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Toast.makeText(mMainActivity, "Error occurred while reading", Toast.LENGTH_SHORT).show();
//                }
//            });
//            // Set the flag to true after reading data
//            is_Read = true;
//        }
//    }
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void sendNotification(String title, String content, String time) {
//        // Get the layouts to use in the custom notification
//        RemoteViews notificationLayout = new RemoteViews(requireContext().getPackageName(),
//                R.layout.layout_custom_notification);
//        notificationLayout.setTextViewText(R.id.title_notification, title);
//        notificationLayout.setTextViewText(R.id.info_notification, content);
//        notificationLayout.setTextViewText(R.id.time_notification, time);
//
//        String ACTION_SNOOZE = "snooze";
//
//        Intent snoozeIntent = new Intent(requireContext(), MainActivity.class);
//        snoozeIntent.setAction(ACTION_SNOOZE);
//        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
//        PendingIntent snoozePendingIntent = PendingIntent.getActivity(requireContext(),
//                0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Add the snooze action button to the custom layout
//        notificationLayout.setOnClickPendingIntent(R.id.button_snooze, snoozePendingIntent);
//
//        Notification notification = new NotificationCompat.Builder(requireContext(), String.valueOf(MyApplication.CHANNEL_ID))
//                .setSmallIcon(R.drawable.timer)
//                .setContent(notificationLayout)
//                .setContentIntent(snoozePendingIntent) // Set the default content intent if the user taps on the notification body
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true)
//                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .build(); // Replace with your own notification icon
//
//        // Get the notification manager
//        NotificationManager notificationManager = (NotificationManager) requireContext()
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // Display the notification
//        if (notificationManager != null) {
//            notificationManager.notify(NOTIFICATION_ID, notification);
//        }
//
//        // Set up a repeating alarm
////        Intent alarmIntent = new Intent(requireContext(), MainActivity.class); // Replace YourAlarmReceiver with your actual AlarmReceiver class
////        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
////                requireContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
//
//        if (alarmManager != null) {
//            alarmManager.setInexactRepeating(
//                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
//                    AlarmManager.INTERVAL_HALF_HOUR,
//                    snoozePendingIntent);
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private long calculateTimeDifference(long hour, long minute) {
//        // initialize timer Variable
//        timervariable timerVariable = new timervariable();
//        Instant currentTime = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            currentTime = Instant.ofEpochMilli(timerVariable.getCurrentTimeInMinutes());
//        }
//        Instant targetTime = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            targetTime = Instant.now()
//                    .atZone(ZoneId.systemDefault())
//                    .withHour((int) hour)
//                    .withMinute((int) minute)
//                    .withSecond(0)
//                    .withNano(0)
//                    .toInstant();
//        }
//
//
//        return Duration.between(currentTime, targetTime).toMinutes();
//
//    }
//
//    private int findNearestActuator(List<actuator> actuatorList) {
//        actuator nearestActuator = null;
//        long minDifference = Long.MAX_VALUE;
//
//        for (actuator actuatorItem : actuatorList) {
//            long currentDifference = 0;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                currentDifference = calculateTimeDifference(actuatorItem.getHour(), actuatorItem.getMinute());
//            }
//            if (currentDifference < minDifference) {
//                minDifference = currentDifference;
//                nearestActuator = actuatorItem;
//            }
//        }
//
//        timeDifferential = minDifference;
//
//        return actuatorList.indexOf(nearestActuator);
//    }
//
//}