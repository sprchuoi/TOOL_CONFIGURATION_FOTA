//package com.example.aquasys.fragment;
//
//import android.annotation.SuppressLint;
//import android.app.AlertDialog;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.viewpager2.widget.ViewPager2;
//
//import android.preference.PreferenceManager;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.aquasys.MainActivity;
//import com.example.aquasys.R;
//import com.example.aquasys.adapter.ActuatorAdapter_add;
//import com.example.aquasys.adapter.ActuatorAdapter_environment;
//import com.example.aquasys.adapter.ActuatorAdapter_water;
//import com.example.aquasys.adapter.TimerActuatorAdapter;
//import com.example.aquasys.object.actuator;
//import com.example.aquasys.viewPagerAdapter;
//import com.example.aquasys.viewPagerAdapter_actuator;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.navigation.NavigationBarView;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.ValueEventListener;
//import com.google.gson.reflect.TypeToken;
//import com.ismaeldivita.chipnavigation.ChipNavigationBar;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import kotlin.Unit;
//import kotlin.jvm.functions.Function1;
//import me.ibrahimsn.lib.OnItemSelectedListener;
//import me.ibrahimsn.lib.SmoothBottomBar;
//
//public class ActuatorFragment extends Fragment {
//
//    private ActuatorAdapter_environment actuatorAdapter_environment ; // adapter for the actuator
//
//    private ActuatorAdapter_water actuatorAdapter_water ; // adapter for the actuator
//    private MainActivity mMainActivity ;
//    private ViewPager2 mViewPager_actuator;
//
//    private SmoothBottomBar navi_menu_actuator;
//
//    ExtendedFloatingActionButton btn_menu_actuator;
//    FloatingActionButton btn_edit_actuator;
//    FloatingActionButton btn_add_actuator;
//
//    // to check whether sub FABs are visible or not
//    Boolean isAllFabsVisible;
//    //create variable for fragment
//
//
//
//    private static final int FRAGMENT_ACTUATOR_ENVIRONMENT = 0;
//    private static final int FRAGMENT_ACTUATOR_WATER = 1;
//
//    private int mCurrentFragment = FRAGMENT_ACTUATOR_ENVIRONMENT;
//    ;
//
//    public ActuatorFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        //actuatorAdapter.notifyDataSetChanged(); // re-change data base on resume method
//
//    }
//
//    @SuppressLint("SetTextI18n")
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate layout for this fragment
//        View mView = inflater.inflate(R.layout.fragment_actuator, container, false);
//        mMainActivity = (MainActivity) getContext();
//        // View pager for actuator fragment
//        mViewPager_actuator = mView.findViewById(R.id.view_pager_actuator);
//        navi_menu_actuator = mView.findViewById(R.id.navi_menu_actuator);
//        // Read from Firebase when the first time the app is opened
////        Read_Data_fromFireBase_Actuator_Tree();
////        Read_Data_fromFireBase_Actuator_Fish();
//        //initialize view pager
//
//
//        // Set up click events for the items in the BottomNavigationView.
//        navi_menu_actuator.setOnItemSelectedListener(new OnItemSelectedListener() {
//            @Override
//            public boolean onItemSelect(int i) {
//                if (i == 0) {
//                    openActuatorFragment_environment();
//                } else if (i == 1) {
//                    openActuatorFragment_water();
//                }
//                return true;
//            }
//
//        });
////        // get list actuator for the first time open the app
////        //for the Actuator environment
////        actuator.globalActuator_environment = mMainActivity.getListActuatorFromSharedPreferences(mMainActivity , "actuator_environment");
////        if (actuator.globalActuator_environment.isEmpty()){
////            Read_Data_fromFireBase_Actuator_Tree();
////        }
////        // for the Actuator water
////        actuator.globalActuator_water = mMainActivity.getListActuatorFromSharedPreferences(mMainActivity , "actuator_water");
////        if (actuator.globalActuator_water.isEmpty()){
////            Read_Data_fromFireBase_Actuator_Fish();
////        }
//
//
//        //setting up viewpager
//        setUpViewPager_adapter();
//
//        return mView;
//    }
//    // Set up the ViewPager with an adapter.
//    private void setUpViewPager_adapter() {
//        viewPagerAdapter_actuator mViewPagerAdapter = new viewPagerAdapter_actuator(this);
//        mViewPager_actuator.setAdapter(mViewPagerAdapter);
//    }
//    //Fragment actuator environment
//    private void openActuatorFragment_environment() {
//        if (mCurrentFragment != FRAGMENT_ACTUATOR_ENVIRONMENT) {
//            mViewPager_actuator.setCurrentItem(0);
//            mCurrentFragment = FRAGMENT_ACTUATOR_ENVIRONMENT;
//            //nếu màn hình hiện tại không ở HomeFragment thì nó sẽ chuyển sang HomeFragment đồng thời lưu giá trị tương ứng vào mCurrentFragment để kiểm tra cho các lần chọn sau
//        }
//    }
//
//
//    //Fragment actuator water
//    private void openActuatorFragment_water() {
//        if (mCurrentFragment != FRAGMENT_ACTUATOR_WATER) {
//            mViewPager_actuator.setCurrentItem(1);
//            mCurrentFragment = FRAGMENT_ACTUATOR_WATER;
//            //nếu màn hình hiện tại không ở HomeFragment thì nó sẽ chuyển sang HomeFragment đồng thời lưu giá trị tương ứng vào mCurrentFragment để kiểm tra cho các lần chọn sau
//        }
//    }
//
//    // Read data of actuator tree
//    public void Read_Data_fromFireBase_Actuator_Tree(){
//        mMainActivity.mDatabaseActuator_environment.addListenerForSingleValueEvent(new ValueEventListener() {
//            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<actuator> actuatorList = new ArrayList<>();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    actuator act = dataSnapshot.getValue(actuator.class);
//                    // check inform in actuator list not return null
//                    if (act != null) {
//                        actuatorList.add(act);
//                    }
//                }
//                actuator.globalActuator_environment = actuatorList;
//                Toast.makeText(mMainActivity, "get list complete !", Toast.LENGTH_SHORT).show();
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(mMainActivity, "Error when reading data", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    public void Read_Data_fromFireBase_Actuator_Fish() {
//        mMainActivity.mDatabaseActuator_water.addListenerForSingleValueEvent(new ValueEventListener() {
//            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<actuator> actuatorList = new ArrayList<>();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    actuator act = dataSnapshot.getValue(actuator.class);
//                    // check inform in actuator list not return null
//                    if (act != null) {
//                        actuatorList.add(act);
//                    }
//                }
//                // test for reading
//                //Toast.makeText(mMainActivity, "Read success", Toast.LENGTH_SHORT).show();
//                actuator.globalActuator_water = actuatorList;
//                actuatorAdapter_water = new ActuatorAdapter_water(actuator.globalActuator_water, (act, position) -> {
//                });
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(mMainActivity, "Error when reading data", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//}