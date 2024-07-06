//package com.example.aquasys.fragment;
//
//import android.annotation.SuppressLint;
//import android.app.AlertDialog;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.view.LayoutInflater;
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
//import com.example.aquasys.object.actuator;
//import com.example.aquasys.system.SharedPreferencesHelper;
//import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.ValueEventListener;
//import com.google.gson.reflect.TypeToken;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class fragment_actuator_water extends Fragment {
//
//    private RecyclerView recyclerview_adapter_water; // RecyclerView for actuator for fish
//    private MainActivity mMainActivity ;
//    private TextView tv_number_actuator_water;
//    private ActuatorAdapter_water actuatorAdapter_water ; // adapter for the actuator
//
//    ExtendedFloatingActionButton btn_menu_actuator_fish;
//    FloatingActionButton btn_edit_actuator_fish;
//    FloatingActionButton btn_add_actuator_fish;
//    Boolean isAllFabsVisible;
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @SuppressLint("SetTextI18n")
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View mView = inflater.inflate(R.layout.fragment_actuator_water, container, false);
//        mMainActivity = (MainActivity) getContext();
//
//        tv_number_actuator_water = mView.findViewById(R.id.tv_number_actuator_water);
//        recyclerview_adapter_water = mView.findViewById(R.id.recyclerview_adapter_water);
//        // check for loading actuator water
//        Read_Data_fromFireBase_Actuator_Fish();
//        // Setting GridLayoutManager with 2 columns for water
//        recyclerview_adapter_water.setLayoutManager(new GridLayoutManager(mMainActivity, 2));
//        // set the actuator in the first time load the recyclerView
//        // mapping button
//        btn_add_actuator_fish  = mView.findViewById(R.id.btn_add_actuator_fish);
//        btn_edit_actuator_fish = mView.findViewById(R.id.btn_edit_actuator_fish);
//        btn_menu_actuator_fish = mView.findViewById(R.id.btn_menu_actuator_fish);
//        int waterActuatorSize = actuator.listActuator_water() != null ? actuator.listActuator_water().size() : 0;
//        tv_number_actuator_water.setText(waterActuatorSize + " Devices");
//        // Now set all the FABs and all the action name
//        // texts as GONE
//        btn_add_actuator_fish.setVisibility(View.GONE);
//        btn_edit_actuator_fish.setVisibility(View.GONE);
//
//        // make the boolean variable as false, as all the
//        // action name texts and all the sub FABs are
//        // invisible
//        isAllFabsVisible = false;
//
//        // Set the Extended floating action button to
//        // shrinked state initially
//        btn_menu_actuator_fish.shrink();
//        btn_menu_actuator_fish.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!isAllFabsVisible) {
//                    // when isAllFabsVisible becomes
//                    // true make all the action name
//                    // texts and FABs VISIBLE.
//                    btn_add_actuator_fish.show();
//                    btn_edit_actuator_fish.show();
//                    // Now extend the parent FAB, as
//                    // user clicks on the shrinked
//                    // parent FAB
//                    btn_menu_actuator_fish.extend();
//                    // make the boolean variable true as
//                    // we have set the sub FABs
//                    // visibility to GONE
//                    isAllFabsVisible = true;
//
//
//                }
//                else {
//                    // when isAllFabsVisible becomes
//                    // true make all the action name
//                    // texts and FABs GONE.
//                    btn_add_actuator_fish.hide();
//                    btn_edit_actuator_fish.hide();
//                    // Set the FAB to shrink after user
//                    btn_menu_actuator_fish.shrink();
//                    // make the boolean variable false
//                    // as we have set the sub FABs
//                    // visibility to GONE
//                    isAllFabsVisible = false;
//                }
//            }
//        });
//        // button edit actuator
//        btn_edit_actuator_fish.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onClick(View v) {
//                // Set actuator list to edit to null
//                actuator.globalActuator_edit = null;
//                AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
//                @SuppressLint("InflateParams") View mViewDialog = mMainActivity.getLayoutInflater().inflate(R.layout.dialog_edit_actuator, null);
//                builder.setView(mViewDialog);
//                builder.setTitle("Edit Actuator");
//                builder.setIcon(R.drawable.edit);
//                // Mapping for components in the dialog layout
//                RecyclerView recyclerview_edit_adapter_water = mViewDialog.findViewById(R.id.recyclerview_edit_adapter);
//                EditText edt_edit_description = mViewDialog.findViewById(R.id.edt_edit_description);
//                TextView description_edit = mViewDialog.findViewById(R.id.description_edit);
//                description_edit.setText(R.string.for_fish);
//                // Set GridLayoutManager for RecyclerView
//                recyclerview_edit_adapter_water.setLayoutManager(new GridLayoutManager(mMainActivity, 2));
//                // Set data for actuator in the timer
//                ActuatorAdapter_add actuatorAdapter_water_add = new ActuatorAdapter_add(actuator.globalActuator_water);
//                // Set RecyclerView with adapter
//                recyclerview_edit_adapter_water.setAdapter(actuatorAdapter_water_add);
//                builder.setPositiveButton("Edit", (dialog, which) -> {
//                    if (actuator.globalActuator_edit != null && actuator.globalActuator_edit.size() == 1) {
//                        actuator editActuator = actuator.globalActuator_edit.get(0);
//                        // Check if the description is empty
//                        if (edt_edit_description.getText().toString().isEmpty()) {
//                            Toast.makeText(mMainActivity, "Please fill in the description and or ID field !", Toast.LENGTH_SHORT).show();
//                            return; // Exit the method if the description is empty
//                        }
//                        // Check the type of the actuator
//                        switch (editActuator.getType()) {
//                            case bulb:
//                                // Update the name and the id in the environment actuator list
//                                actuator.globalActuator_environment.get(mMainActivity.pos_edit_actuator).setName(edt_edit_description.getText().toString());
//                                break;
//                            case pump:
//                            case heater:
//                            case feeder:
//                                // Update the name and the id in the environment actuator list
//                                actuator.globalActuator_water.get(mMainActivity.pos_edit_actuator).setName(edt_edit_description.getText().toString());
//                                break;
//                            // Add more cases if there are other types
//                            default:
//                                break;
//                        }
//                        // Notify Firebase about the changes
//                        mMainActivity.addActuatorToFireBase();
//                        actuatorAdapter_water.notifyDataSetChanged();
//
//                    } else {
//                        Toast.makeText(mMainActivity, "Please choose an adapter again, select only 1 adapter!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                mMainActivity.pos_edit_actuator = 0;
//                builder.setNegativeButton("Cancel", null);
//
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//        });
//
//
//        // button add actuator
//        btn_add_actuator_fish.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onClick(View v) {
//                // Set actuator list to add to null
//                actuator.globalActuator_add = null;
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
//                @SuppressLint("InflateParams") View mViewDialog = mMainActivity.getLayoutInflater().inflate(R.layout.dialog_add_actuator, null);
//                builder.setView(mViewDialog);
//                builder.setTitle("Add actuator");
//                builder.setIcon(R.drawable.add);
//                // Mapping for components in the dialog layout
//                RecyclerView recyclerview_add_adapter_water = mViewDialog.findViewById(R.id.recyclerview_add_adapter);
//                TextView description_add = mViewDialog.findViewById(R.id.description_add);
//                EditText edt_add_description = mViewDialog.findViewById(R.id.edt_add_description);
//                EditText edt_add_ID  = mViewDialog.findViewById(R.id.edt_add_ID);
//                description_add.setText(R.string.for_fish);
//                // Set GridLayoutManager for RecyclerView
//                recyclerview_add_adapter_water.setLayoutManager(new GridLayoutManager(mMainActivity, 2));
//                // Set data for actuator in the timer
//                ActuatorAdapter_add actuatorAdapter_water_add = new ActuatorAdapter_add(actuator.listActuator_water_add());
//                // Set RecyclerView with adapter
//                recyclerview_add_adapter_water.setAdapter(actuatorAdapter_water_add);
//                builder.setPositiveButton("Add", (dialog, which) -> {
//                    if (actuator.globalActuator_add != null && actuator.globalActuator_add.size() == 1) {
//                        if (!edt_add_description.getText().toString().isEmpty() && !edt_add_ID.getText().toString().isEmpty()) {
//                            actuator.globalActuator_add.get(0).setName(edt_add_description.getText().toString());
//                            actuator.globalActuator_add.get(0).setId(edt_add_ID.getText().toString());
//                            actuator.globalActuator_water.add(actuator.globalActuator_add.get(0));
//                            actuatorAdapter_water = new ActuatorAdapter_water(actuator.globalActuator_water, (act, position) -> {
//                            });
//                            recyclerview_adapter_water.setAdapter(actuatorAdapter_water);
//                            //save actuator to firebase
//                            mMainActivity.addActuatorToFireBase();
//                            actuatorAdapter_water.notifyDataSetChanged();
//                            int waterActuatorSize = actuator.listActuator_water() != null ? actuator.listActuator_water().size() : 0;
//                            tv_number_actuator_water.setText(waterActuatorSize + " Devices");
//                            SharedPreferencesHelper.saveListToSharedPreferences(mMainActivity, actuator.globalActuator_water, "actuator_water");
//                        } else {
//                            // Display a toast when the conditions are not met
//                            Toast.makeText(mMainActivity, "Please fill in the name  and the ID field!", Toast.LENGTH_SHORT).show();
//                         }
//                    } else {
//                        Toast.makeText(mMainActivity, "Please choose an adapter again, select only 1 adapter!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                builder.setNegativeButton("Cancel", null);
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//        });
//        return mView;
//    }
//    private boolean is_Read = false;
//
//    public void Read_Data_fromFireBase_Actuator_Fish() {
//        actuatorAdapter_water = new ActuatorAdapter_water(actuator.globalActuator_water, (act, position) -> {
//        });
//        recyclerview_adapter_water.setAdapter(actuatorAdapter_water);
//        int waterActuatorSize = actuator.globalActuator_water.size();
//        tv_number_actuator_water.setText(waterActuatorSize + " Devices");
//        mMainActivity.mDatabaseActuator_water.addValueEventListener(new ValueEventListener() {
//
//            @SuppressLint("NotifyDataSetChanged")
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
//                int environmentActuatorSize = actuatorList.size();
//                tv_number_actuator_water.setText(environmentActuatorSize + " Devices");
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(mMainActivity, "Error when reading data", Toast.LENGTH_SHORT).show();
//            }
//
//        });
//    }
//}