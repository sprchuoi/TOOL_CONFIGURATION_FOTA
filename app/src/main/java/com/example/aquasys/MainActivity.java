package com.example.aquasys;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.aquasys.login.Login;
import com.example.aquasys.network.NotificationBroadcastReceiver;
import com.example.aquasys.object.actuator;
import com.example.aquasys.object.nodeSensor;
import com.example.aquasys.object.sensor;
import com.example.aquasys.object.timer;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int FRAGMENT_SENSOR = 0;
    private static final int FRAGMENT_FUOTA = 1;
    //private static final String CHANNEL_ID = "notify";
    private final StringBuilder text = new StringBuilder();
    public DatabaseReference mDatabaseNodeSensor;

    public DatabaseReference mDatabaseSensor_Data;

    public DatabaseReference mDatabaseFirmware;
    //public DatabaseReference mDatabaseSensor_water;
//    public DatabaseReference mDatabaseActuator_environment;
//    public DatabaseReference mDatabaseActuator_water;
//    public DatabaseReference mDatabaseSchedule;
//    public DatabaseReference mDatabaseSensor_val;
//    public DatabaseReference mDatabaseStatus_Actuator_environment;
//    public DatabaseReference mDatabaseStatus_Actuator_water;
//    public int pos_edit_actuator;
//    public actuator tmp_actuator;
    public FirebaseFirestore db;
//    public CollectionReference actuatorCollection;
//    public File logFile;
    public String email_name;
    public FirebaseDatabase database;

    //------realtime for update timer wakeup -----//

    BroadcastReceiver broadcastReceiver;
    FirebaseAuth auth;
    FirebaseUser user;
    private int mCurrentFragment = FRAGMENT_SENSOR;
    private ChipNavigationBar bottom_navi;
    private ViewPager2 mViewPager; // Declare a ViewPager2 variable.
    private DrawerLayout drawer_layout; // Declare a DrawerLayout variable.
    private static final String ONESIGNAL_APP_ID = "29885265-0db1-43c7-a5f3-b7e1e842aaec"; // declare Notify from another app
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check Internet Connection
        // create broadcast receiver

        broadcastReceiver = new NotificationBroadcastReceiver();
        registerNetworkBroadcastReceiver();
        // Check if the user is authenticated
        getUserInformation();
        // Verbose Logging set to help debug issues, remove before releasing your app.
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        // OneSignal Initialization
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        OneSignal.getNotifications().requestPermission(false, Continue.none());
        bottom_navi = findViewById(R.id.bottom_navi); // Find the BottomNavigationView in the layout.
        mViewPager = findViewById(R.id.view_pager); // Find the ViewPager2 in the layout.
        drawer_layout = findViewById(R.id.drawer_layout); // Find the DrawerLayout in the layout
        // Set up the ViewPager for handling swipeable screens.
        setUpViewPager();
        // Set up the Toolbar and connect it to the ActionBar.
        // Declare a Toolbar variable.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Right side button toolbar
        Button btn_update = new Button(this);
        btn_update.setBackgroundResource(R.drawable.update);
        Toolbar.LayoutParams l3 = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        btn_update.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
        l3.gravity = Gravity.END;
        l3.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()); // Set width to 60dp
        l3.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()); // Set height to 60dp
        btn_update.setLayoutParams(l3);
        //add Firebase firestore
        db = FirebaseFirestore.getInstance();
        //actuatorCollection = db.collection("actuators");
        toolbar.addView(btn_update);
        btn_update.setOnClickListener(v -> {
           sync_Data();
        });

        // Create an ActionBarDrawerToggle for syncing the ActionBar with the DrawerLayout.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle); // Add a DrawerListener to the DrawerLayout.
        toggle.syncState(); // Synchronize the ActionBarDrawerToggle state with the DrawerLayout.
        // Set up click events for the NavigationView.
        NavigationView navigation_view = findViewById(R.id.navigation_view);
        navigation_view.setNavigationItemSelectedListener(this);
        // Set up click events for the items in the BottomNavigationView.
        bottom_navi.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                if (id == R.id.active_sensor) {
                    openSensorFragment();
                } else if (id == R.id.active_FUOTA) {
                    openUpdateManagerFragment();
                }
//                  else if (id == R.id.active_log) {
//                      saveDataCloudtoLog();
//                      openLogFragment();
//                  }
            }
        });
        // Set up a callback for ViewPager page changes.
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update the selected item in both the NavigationView and BottomNavigationView.
                switch (position) {
                    case 0:
                        mCurrentFragment = FRAGMENT_SENSOR; //sửa trạng thái của biến mCurrentFragment thành FRAGMENT_SENSOR
                        navigation_view.getMenu().findItem(R.id.active_sensor).setChecked(true);
                        bottom_navi.setItemSelected(navigation_view.getMenu().findItem(R.id.active_sensor).getItemId(), true);
                        break;
                    case 1:
                        mCurrentFragment = FRAGMENT_FUOTA; //sửa trạng thái của biến mCurrentFragment thành FRAGMENT_FUOTA
                        navigation_view.getMenu().findItem(R.id.active_FUOTA).setChecked(true);
                        bottom_navi.setItemSelected(navigation_view.getMenu().findItem(R.id.active_FUOTA).getItemId(), true);
                        break;
//                    case 2:
//                        mCurrentFragment = FRAGMENT_TIMER; //sửa trạng thái của biến mCurrentFragment thành FRAGMENT_TIMER
//                        navigation_view.getMenu().findItem(R.id.active_timer).setChecked(true);
//                        bottom_navi.setItemSelected(navigation_view.getMenu().findItem(R.id.active_timer).getItemId(), true);
//                        break;
//                    case 3:
//                        mCurrentFragment = FRAGMENT_LOG; //sửa trạng thái của biến mCurrentFragment thành FRAGMENT_TIMER
//                        navigation_view.getMenu().findItem(R.id.active_log).setChecked(true);
//                        bottom_navi.setItemSelected(navigation_view.getMenu().findItem(R.id.active_log).getItemId(),true);
//                        break;
                }
            }
        });

        // database Sensor
        mDatabaseNodeSensor = FirebaseDatabase.getInstance().getReference().child("SensorNode");
        mDatabaseSensor_Data = FirebaseDatabase.getInstance().getReference().child("DataSensor");
        mDatabaseFirmware =FirebaseDatabase.getInstance().getReference().child("Firmware");
//        // Assuming you have a reference to the Firebase Realtime Database
//        if (isLoginFirstTime()) {
//            setFirstTimeFlag(true);
//            //sync_Data();
//        }
    }

    // sync data

    public void sync_Data() {
        addNodeSensorToFireBase();
        addDataSensortoFirebase();
        Toast.makeText(MainActivity.this, "Sync data success !! ", Toast.LENGTH_SHORT).show();
    }

    // Manage user information
    @SuppressLint("SetTextI18n")
    private void getUserInformation() {
        // Method to check user authentication and redirect to the login page if necessary
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        // get user
        //Firebase
        user = auth.getCurrentUser();
        // Display in the email
        NavigationView navigation_view = (NavigationView) findViewById(R.id.navigation_view);
        if (user == null) {
            // User is not logged in, so redirect to the login page
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            email_name = Objects.requireNonNull(user).getEmail();
            TextView headerTextView = navigation_view.getHeaderView(0).findViewById(R.id.header_email); // Replace with the actual ID of your header TextView
            headerTextView.setText(email_name); // Set the text to the desired value
        }
    }

    // Set up the ViewPager with an adapter.
    private void setUpViewPager() {
        viewPagerAdapter mViewPagerAdapter = new viewPagerAdapter(this);
        mViewPager.setAdapter(mViewPagerAdapter);
    }

    // Handle item clicks in the NavigationView and synchronize with the BottomNavigationView.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.active_sensor) {
            openSensorFragment();
            bottom_navi.setItemSelected(id, true);
        } else if (id == R.id.active_FUOTA) {
            openUpdateManagerFragment();
            bottom_navi.setItemSelected(id, true);
        }
//        else if (id == R.id.active_log) {
//            saveDataCloudtoLog();
//            openLogFragment();
//            bottom_navi.setItemSelected(id,true);
//      }
       else if (id == R.id.logout) {
            // Sign out the user and redirect to the login page
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "Logout success", Toast.LENGTH_SHORT).show();
            finish();
        }
        else if (id == R.id.change_password) {
            Intent intent = new Intent(getApplicationContext(), ChangePassword.class);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "Return MainActivity", Toast.LENGTH_SHORT).show();
        }
//        else if (id == R.id.export) {
//            saveDataCloudtoLog();
//            // Send log file via email (replace with your email sending logic)
//            sendLogFileByEmail(logFile, "20119063@student.hcmute.edu.vn");
//            Toast.makeText(MainActivity.this, "Export Complete", Toast.LENGTH_SHORT).show();
//        }

        drawer_layout.closeDrawer(GravityCompat.START); // Close the navigation drawer.
        return true;
    }

//    public void goToSensorDevice(String nameSensor, int index, String typeSensor) { //tạo phương thức chuyển hướng Fragment từ Room sang Device
//        //tạo Intent để chuyển từ MainActivity sang Device Activity đồng thời bundle dữ liệu sang
//        Intent i = new Intent(this, SensorDevice.class);
//        Bundle bundle = new Bundle();
//        bundle.putString("nameSensor", nameSensor);
//        bundle.putString("typeSensor", typeSensor);
//        bundle.putInt("index", index);
//        i.putExtras(bundle);
//        //chuyển dữ liệu gồm tên phòng (key:NameRoom) và vị trí của phòng (key:index)
//        startActivity(i);
//    }
    //Create method open Fragment
    //Fragment Sensor


    //Fragment Actuator
    private void openUpdateManagerFragment() {
        if (mCurrentFragment != FRAGMENT_FUOTA) {
            mViewPager.setCurrentItem(1);
            mCurrentFragment = FRAGMENT_FUOTA;
        }
    }
    //Create method open Fragment
    //Fragment Sensor
    private void openSensorFragment() {
        if (mCurrentFragment != FRAGMENT_SENSOR) {
            mViewPager.setCurrentItem(0);
            mCurrentFragment = FRAGMENT_SENSOR;
            //nếu màn hình hiện tại không ở HomeFragment thì nó sẽ chuyển sang HomeFragment đồng thời lưu giá trị tương ứng vào mCurrentFragment để kiểm tra cho các lần chọn sau
        }
    }
    // Handle the back button press to close the navigation drawer if it's open.
    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    // save sensor to firebase
    public void addNodeSensorToFireBase() {
        // sensor environment
        mDatabaseNodeSensor.setValue(nodeSensor.listNode()).addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                });
    }
    public void UpDateStatus_updateToFireBase() {
        // sensor environment
        mDatabaseFirmware.child("update_status").setValue("true").addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                });
    }

    public void UpDateStatus_updateToFireBase_False() {
        // sensor environment
        mDatabaseFirmware.child("update_status").setValue("false").addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                });
    }


    public void UpdateVersionFirebase(String Node_Address , int Version_main , int Version_sub){
        for(int i=0 ; i<nodeSensor.listNode().size() ;i++){
            if(Objects.equals(nodeSensor.listNode().get(i).getAddressSensor(), Node_Address)){
                mDatabaseNodeSensor.child(String.valueOf(i)).child("applVersion_main").setValue(Version_main).addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                            // Handle any errors
                            Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                        });
                mDatabaseNodeSensor.child(String.valueOf(i)).child("applVersion_sub").setValue(Version_sub).addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                            // Handle any errors
                            Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                        });
            }

        }
    }
    public void addDataSensortoFirebase(){
        for(int i =0 ; i <nodeSensor.listNode().size(); i++) {
            //Sensor Node ADD1
            mDatabaseSensor_Data.child(nodeSensor.listNode().get(i).getAddressSensor()).child("value1").setValue("1").addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                        Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                    });
            mDatabaseSensor_Data.child(nodeSensor.listNode().get(i).getAddressSensor()).child("value2").setValue("1").addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                        Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                    });
            mDatabaseSensor_Data.child(nodeSensor.listNode().get(i).getAddressSensor()).child("value3").setValue("1").addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                        Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                    });
            mDatabaseSensor_Data.child(nodeSensor.listNode().get(i).getAddressSensor()).child("value4").setValue("1").addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                        Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    public String formatFirebaseDateTime(String firebaseDateTime) {
        SimpleDateFormat firebaseDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        try {
            if (firebaseDateTime !=null) {
                Date date = firebaseDateFormat.parse(firebaseDateTime);
                if(date !=null)
                    return displayDateFormat.format(date);
            }// error
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // hoặc trả về một chuỗi mặc định nào đó
        }
    }




    // save actuator to firebase
//    public void addActuatorToFireBase() {
//        //actuator for tree
//        mDatabaseActuator_environment.setValue(actuator.listActuator_environment()).addOnSuccessListener(aVoid -> {
//                    // Data has been saved successfully
//                })
//                .addOnFailureListener(e -> {
//                    // Handle any errors
//                    Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
//                });
//        //actuator for fish
//        mDatabaseActuator_water.setValue(actuator.listActuator_water()).addOnSuccessListener(aVoid -> {
//                    // Data has been saved successfully
//                })
//                .addOnFailureListener(e -> {
//                    // Handle any errors
//                    Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
//                });
//    }

    // save Schedule to firebase
//    public void addScheduleToFireBase() {
//        mDatabaseSchedule.setValue(timer.globalTimer).addOnSuccessListener(aVoid -> {
//                    // Data has been saved successfully
//                })
//                .addOnFailureListener(e -> {
//                    // Handle any errors
//                    Toast.makeText(MainActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
//                });
//    }

    // MetWork processing when occur error connection
    // register Network
    protected void registerNetworkBroadcastReceiver() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

//    // store to log active actuator to cloud fireabase
//    public void sendActuatorToFireBase_water(int position) {
//        actuator act = actuator.globalActuator_water.get(position);
//        // Create a Map with the specific fields you want to push
//        Map<String, Object> actuatorData = new HashMap<>();
//        actuatorData.put("name", act.getName());
//        actuatorData.put("ID", act.getId());
//        actuatorData.put("status", act.getStatus());
//        actuatorData.put("hour", act.getHour());
//        actuatorData.put("Img", act.getImg());
//        // Reference to the Firestore collection
//        CollectionReference actuatorCollection = FirebaseFirestore.getInstance().collection("actuators");
//
//        // Use a specific document ID, for example, the actuator's name
//        String timeDate_log = String.valueOf(new Date());
//
//        // Add the actuator data to Firestore with a specific document ID
//        actuatorCollection.document(timeDate_log).set(actuatorData)
//                .addOnSuccessListener(documentReference -> {
//                    // Handle success
//                })
//                .addOnFailureListener(e -> {
//                    // Handle failure
//                });
//    }
//
//    // send actuator to Firebase
//    public void sendActuatorToFireBase_environment(int position) {
//        actuator act = actuator.globalActuator_environment.get(position);
//        String timeDate_log = String.valueOf(new Date());
//        // Create a Map with the specific fields you want to push
//        Map<String, Object> actuatorData = new HashMap<>();
//        actuatorData.put("name", act.getName());
//        actuatorData.put("ID", act.getId());
//        actuatorData.put("status", act.getStatus());
//        actuatorData.put("hour", act.getHour());
//        actuatorData.put("Img", act.getImg());
//        actuatorData.put("minute", act.getMinute());
//        // Reference to the Firestore collection
//        CollectionReference actuatorCollection = FirebaseFirestore.getInstance().collection("actuators");
//        // Use a specific document ID, for example, the actuator's name
//        // Add the actuator data to Firestore with a specific document ID
//        actuatorCollection.document(timeDate_log).set(actuatorData)
//                .addOnSuccessListener(documentReference -> {
//                    // Handle success
//                })
//                .addOnFailureListener(e -> {
//                    // Handle failure
//                });
//    }
//
//    // load file log to textview
//    public StringBuilder loadAndDisplayLog() {
//        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
//            // Clear existing text
//            text.setLength(0);
//            // Read file content
//            String mLine;
//            while ((mLine = reader.readLine()) != null) {
//                text.append(mLine);
//                text.append('\n');
//            }
//        } catch (IOException e) {
//            Toast.makeText(MainActivity.this, "Error reading file!", Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        }
//        return text;
//    }

//    public void saveDataCloudtoLog() {
//        // Reference to the Firestore collection
//        CollectionReference actuatorCollection = FirebaseFirestore.getInstance().collection("actuators");
//        // Query all documents in the collection
//        // Handle failure
//        actuatorCollection.get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    StringBuilder logData = new StringBuilder(); // StringBuilder to store log data
//
//                    // Iterate through the documents
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        // Convert the document to your Actuator object or retrieve data
//                        String time = document.getId();
//                        String name = document.getString("name");
//                        String ID = document.getString("ID");
//                        int status = Objects.requireNonNull(document.getLong("status")).intValue();
//                        int hour = Objects.requireNonNull(document.getLong("hour")).intValue();
//                        int minute = Objects.requireNonNull(document.getLong("minute")).intValue();
//                        if (status == 1) {
//                            // Append data to StringBuilder
//                            logData.append(time)
//                                    .append(" Name: ").append(name)
//                                    .append(", ID: ").append(ID)
//                                    .append(", Status: ").append(status)
//                                    .append(", Duration : ").append(hour).append("h:").append(minute).append("min")
//                                    .append("\n");
//                        } else {
//                            // Append data to StringBuilder
//                            logData.append(time)
//                                    .append(" Name: ").append(name)
//                                    .append(", ID: ").append(ID)
//                                    .append(", Status: ").append(status)
//                                    .append("\n");
//                        }
//                    }
//                    // Save log data to a file
//                    try {
//                        logFile = saveLogToFile(logData.toString());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                })
//                .addOnFailureListener(Throwable::printStackTrace);
//    }
//
//    // Method to save log data to a file
//    public File saveLogToFile(String logData) throws IOException {
//        File backupPath = Environment.getExternalStorageDirectory();
//        if (!backupPath.exists()) {
//            backupPath.mkdirs();
//        }
//        File logFile = new File(backupPath.getPath() + "/record_activate.txt");
//        try (FileOutputStream fos = new FileOutputStream(logFile)) {
//            fos.write(logData.getBytes());
//            Toast.makeText(this, "Write file complete", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        return logFile;
//    }

    // Method to send log file via email (replace with your email sending logic)
    // Method to send log file via email (replace with your email sending logic)
//    @SuppressLint("QueryPermissionsNeeded")
//    private void sendLogFileByEmail(File logFile, String toEmail) {
//        // Create an email intent
//        Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        emailIntent.setType("vnd.android.cursor.dir/email");
//        String[] to = {toEmail};
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Actuator Log Data");
//        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached the log file.");
//        // Get content URI using FileProvider
//        Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", logFile);
//        emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
//        //emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read permission
//        // Check if there's an email client available to handle the intent
//        startActivity(Intent.createChooser(emailIntent, "Send email..."));
//
//    }

    protected void unregisterNetwork() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }

    // push val sensor to firebase
//    public void pushSensorVal() {
//        mDatabaseSensor_val.child("Humi").setValue(sensor.globalSensor_enviroment.get(0).getValue());
//        mDatabaseSensor_val.child("Temp").setValue(sensor.globalSensor_enviroment.get(1).getValue());
//        mDatabaseSensor_val.child("Light").setValue(sensor.globalSensor_enviroment.get(2).getValue());
//        mDatabaseSensor_val.child("Moisture").setValue(sensor.globalSensor_enviroment.get(3).getValue());
//        mDatabaseSensor_val.child("pH").setValue(sensor.globalSensor_water.get(0).getValue());
//        mDatabaseSensor_val.child("Water_level").setValue(sensor.globalSensor_water.get(1).getValue());
//    }

    // get actuator list
//    public List<actuator> getListActuatorFromSharedPreferences(Context context, String actuator_type) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        String actuatorJson = sharedPreferences.getString(actuator_type, "");
//
//        if (!actuatorJson.isEmpty()) {
//            Type type = new TypeToken<List<actuator>>() {
//            }.getType();
//            return new Gson().fromJson(actuatorJson, type);
//        }
//
//        Toast.makeText(context, "get actuator", Toast.LENGTH_SHORT).show();
//        return new ArrayList<>(); // Return an empty list if no data is found
//    }
//
//    // add list  actuator to local
//    public void saveListActuatorToSharedPreferences(Context context, List<actuator> actuatorList, String save_name) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        String actuatorJson = new Gson().toJson(actuatorList);
//        editor.putString(save_name, actuatorJson);
//        editor.apply();
//    }
//
//    // for the schedule
//    public List<timer> getListTimerFromSharedPreferences(Context context, String timer_type) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        String timerJson = sharedPreferences.getString(timer_type, "");
//
//        if (!timerJson.isEmpty()) {
//            Type type = new TypeToken<List<timer>>() {
//            }.getType();
//            return new Gson().fromJson(timerJson, type);
//        }
//
//        return new ArrayList<>(); // Return an empty list if no data is found
//    }
//
//    // add list  actuator to local
//    public void saveListTimerToSharedPreferences(Context context, List<actuator> timerList, String save_name) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        String actuatorJson = new Gson().toJson(timerList);
//        editor.putString(save_name, actuatorJson);
//        editor.apply();
//    }
//
//    // for the actuator
//    public List<sensor> getListSensorFromSharedPreferences(Context context, String sensor_type) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        String sensorJson = sharedPreferences.getString(sensor_type, "");
//
//        if (!sensorJson.isEmpty()) {
//            Type type = new TypeToken<List<sensor>>() {
//            }.getType();
//            return new Gson().fromJson(sensorJson, type);
//        }
//
//        return new ArrayList<>(); // Return an empty list if no data is found
//    }
//
//    // add list  actuator to local
//    public void saveListSensorToSharedPreferences(Context context, List<actuator> sensorList, String save_name) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        String sensorJson = new Gson().toJson(sensorList);
//        editor.putString(save_name, sensorJson);
//        editor.apply();
//    }

//    public void pushActuatorListToFirebase(List<actuator> actuatorList) {
//        // Push actuatorList to Firebase using your preferred method
//        // Example: mDatabaseActuatorWater.setValue(actuatorList);
//    }

//    private boolean isLoginFirstTime() {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        return sharedPreferences.getBoolean(user.getUid(), true);
//    }
//
//    private void setFirstTimeFlag(boolean value) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(user.getUid(), value);
//        editor.apply();
//    }
//    public  void get_RTCWakeup_timer(){
//        Date currentTime = Calendar.getInstance().getTime();
//
//
//    }
//    //start service
//    public void startService(View v){
//
//    }

}