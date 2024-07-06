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
//public class fragment_actuator_environment extends Fragment {
//
//    private MainActivity mMainActivity;
//    private RecyclerView recyclerview_adapter_environment ; // recyclerView for sensor
//    private ActuatorAdapter_environment actuatorAdapter_environment;
//    private  TextView tv_number_actuator_environment;
//
//    ExtendedFloatingActionButton btn_menu_actuator_tree;
//    FloatingActionButton btn_edit_actuator_tree;
//    FloatingActionButton btn_add_actuator_tree;
//    Boolean isAllFabsVisible;
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        // Inflate the layout for this fragment.
//        // A reference to the main view of the fragment.
//        View mView = inflater.inflate(R.layout.fragment_actuator_environment, container, false); // Inflate the fragment_home layout into mView.
//        // mapping button
//        btn_add_actuator_tree = mView.findViewById(R.id.btn_add_actuator_tree);
//        btn_edit_actuator_tree = mView.findViewById(R.id.btn_edit_actuator_tree);
//        btn_menu_actuator_tree = mView.findViewById(R.id.btn_menu_actuator_tree);
//        mMainActivity = (MainActivity) getActivity(); // Get a reference to the hosting Activity (assumed to be MainActivity).
//
//        recyclerview_adapter_environment = mView.findViewById(R.id.recyclerview_adapter_environment); // Find the RecyclerView in the layout.
//        tv_number_actuator_environment =  mView.findViewById(R.id.tv_number_actuator_environment);
//        // check for loading actuator environment
//        Read_Data_fromFireBase_Actuator_Tree();
//        //Setting GridLayoutManager with 2 columns for environment
//        recyclerview_adapter_environment.setLayoutManager(new GridLayoutManager(mMainActivity, 2));
//        // Assuming there is a similar list for environment actuators
//        int environmentActuatorSize = actuator.listActuator_environment() != null ? actuator.globalActuator_environment.size() : 0;
//        tv_number_actuator_environment.setText(environmentActuatorSize + " Devices");
//        // set list for sensor adapter
//        // Now set all the FABs and all the action name
//        // texts as GONE
//        btn_add_actuator_tree.setVisibility(View.GONE);
//        btn_edit_actuator_tree.setVisibility(View.GONE);
//        // make the boolean variable as false, as all the
//        // action name texts and all the sub FABs are
//        // invisible
//        isAllFabsVisible = false;
//        // Set the Extended floating action button to
//        // shrinked state initially
//        btn_menu_actuator_tree.shrink();
//        btn_menu_actuator_tree.setOnClickListener(v -> {
//            if (!isAllFabsVisible) {
//                // when isAllFabsVisible becomes
//                // true make all the action name
//                // texts and FABs VISIBLE.
//                btn_add_actuator_tree.show();
//                btn_edit_actuator_tree.show();
//                // Now extend the parent FAB, as
//                // user clicks on the shrinked
//                // parent FAB
//                btn_menu_actuator_tree.extend();
//                // make the boolean variable true as
//                // we have set the sub FABs
//                // visibility to GONE
//                isAllFabsVisible = true;
//            }
//            else {
//                // when isAllFabsVisible becomes
//                // true make all the action name
//                // texts and FABs GONE.
//                btn_add_actuator_tree.hide();
//                btn_edit_actuator_tree.hide();
//                // Set the FAB to shrink after user
//                btn_menu_actuator_tree.shrink();
//                // make the boolean variable false
//                // as we have set the sub FABs
//                // visibility to GONE
//                isAllFabsVisible = false;
//            }
//        });
//        // button edit actuator
//        btn_edit_actuator_tree.setOnClickListener(v -> {
//            // Set actuator list to edit to null
//            actuator.globalActuator_edit = null;
//            AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
//            @SuppressLint("InflateParams") View mViewDialog = mMainActivity.getLayoutInflater().inflate(R.layout.dialog_edit_actuator, null);
//            builder.setView(mViewDialog);
//            builder.setTitle("Edit Actuator");
//            builder.setIcon(R.drawable.edit);
//            // Mapping for components in the dialog layout
//            RecyclerView recyclerview_edit_adapter_environment = mViewDialog.findViewById(R.id.recyclerview_edit_adapter);
//            EditText edt_edit_description = mViewDialog.findViewById(R.id.edt_edit_description);
//            TextView description_edit = mViewDialog.findViewById(R.id.description_edit);
//            description_edit.setText(R.string.for_environment);
//            // Set GridLayoutManager for RecyclerView
//            recyclerview_edit_adapter_environment.setLayoutManager(new GridLayoutManager(mMainActivity, 2));
//            // Set data for actuator in the timer
//            ActuatorAdapter_add actuatorAdapter_environment_add = new ActuatorAdapter_add(actuator.globalActuator_environment);
//            // Set RecyclerView with adapter
//            recyclerview_edit_adapter_environment.setAdapter(actuatorAdapter_environment_add);
//            builder.setPositiveButton("Edit", (dialog, which) -> {
//                if (actuator.globalActuator_edit != null && actuator.globalActuator_edit.size() == 1) {
//                    actuator editActuator = actuator.globalActuator_edit.get(0);
//                    // Check if the description is empty
//                    if (edt_edit_description.getText().toString().isEmpty()) {
//                        Toast.makeText(mMainActivity, "Please fill in the description and or ID field !", Toast.LENGTH_SHORT).show();
//                        return; // Exit the method if the description is empty
//                    }
//                    // Check the type of the actuator
//                    switch (editActuator.getType()) {
//                        case bulb:
//                            // Update the name and the id in the environment actuator list
//                            actuator.globalActuator_environment.get(mMainActivity.pos_edit_actuator).setName(edt_edit_description.getText().toString());
//                            break;
//                        case pump:
//                        case heater:
//                        case feeder:
//                            // Update the name and the id in the environment actuator list
//                            actuator.globalActuator_water.get(mMainActivity.pos_edit_actuator).setName(edt_edit_description.getText().toString());
//                            break;
//                        // Add more cases if there are other types
//                        default:
//                            break;
//                    }
//                    // Notify Firebase about the changes
//                    mMainActivity.addActuatorToFireBase();
//                    actuatorAdapter_environment.notifyDataSetChanged();
//
//                } else {
//                    Toast.makeText(mMainActivity, "Please choose an adapter again, select only 1 adapter!", Toast.LENGTH_SHORT).show();
//                }
//            });
//            mMainActivity.pos_edit_actuator = 0;
//            builder.setNegativeButton("Cancel", null);
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        });
//        // button add actuator
//        btn_add_actuator_tree.setOnClickListener(v -> {
//            // Set actuator list to add to null
//            actuator.globalActuator_add = null;
//            AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
//            @SuppressLint("InflateParams") View mViewDialog = mMainActivity.getLayoutInflater().inflate(R.layout.dialog_add_actuator, null);
//            builder.setView(mViewDialog);
//            builder.setTitle("Add actuator");
//            builder.setIcon(R.drawable.add);
//            // Mapping for components in the dialog layout
//            RecyclerView recyclerview_add_adapter_environment = mViewDialog.findViewById(R.id.recyclerview_add_adapter);
//            TextView description_add = mViewDialog.findViewById(R.id.description_add);
//            EditText edt_add_description = mViewDialog.findViewById(R.id.edt_add_description);
//            EditText edt_add_ID  = mViewDialog.findViewById(R.id.edt_add_ID);
//            description_add.setText(R.string.for_environment);
//            // Set GridLayoutManager for RecyclerView
//            recyclerview_add_adapter_environment.setLayoutManager(new GridLayoutManager(mMainActivity, 2));
//            // Set data for actuator in the timer
//            ActuatorAdapter_add actuatorAdapter_environment_add = new ActuatorAdapter_add(actuator.listActuator_environment_add());
//            // Set RecyclerView with adapter
//            recyclerview_add_adapter_environment.setAdapter(actuatorAdapter_environment_add);
//            builder.setPositiveButton("Add", (dialog, which) -> {
//                if (actuator.globalActuator_add != null && actuator.globalActuator_add.size() == 1) {
//                    if (!edt_add_description.getText().toString().isEmpty() && !edt_add_ID.getText().toString().isEmpty()) {
//                        actuator.globalActuator_add.get(0).setName(edt_add_description.getText().toString());
//                        actuator.globalActuator_add.get(0).setId(edt_add_ID.getText().toString());
//                        actuator.globalActuator_environment.add(actuator.globalActuator_add.get(0));
//                        actuatorAdapter_environment = new ActuatorAdapter_environment(actuator.globalActuator_environment, (act, position) -> {
//                        });
//                        recyclerview_adapter_environment.setAdapter(actuatorAdapter_environment);
//                        //save actuator to firebase
//                        mMainActivity.addActuatorToFireBase();
//                        actuatorAdapter_environment.notifyDataSetChanged();
//                        int environmentActuatorSize1 = actuator.listActuator_environment() != null ? actuator.globalActuator_environment.size() : 0;
//                        tv_number_actuator_environment.setText(environmentActuatorSize1 + " Devices");
//                        com.example.aquasys.system.SharedPreferencesHelper.saveListToSharedPreferences(mMainActivity, actuator.globalActuator_environment, "actuator_environment");
//                    } else {
//                        // Display a toast when the conditions are not met
//                        Toast.makeText(mMainActivity, "Please fill in the name  and the ID field!", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(mMainActivity, "Please choose an adapter again, select only 1 adapter!", Toast.LENGTH_SHORT).show();
//                }
//            });
//            builder.setNegativeButton("Cancel", null);
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        });
//        // Display the number of environment actuators in the list
//        return mView;
//    }
//    // Read data of actuator tree
//    @SuppressLint("SetTextI18n")
//    public void Read_Data_fromFireBase_Actuator_Tree(){
//        actuatorAdapter_environment = new ActuatorAdapter_environment(actuator.globalActuator_environment, (act, position) -> {
//        });
//        recyclerview_adapter_environment.setAdapter(actuatorAdapter_environment);
//        int environmentActuatorSize = actuator.globalActuator_environment.size();
//        tv_number_actuator_environment.setText(environmentActuatorSize + " Devices");
//        mMainActivity.mDatabaseActuator_environment.addValueEventListener(new ValueEventListener() {
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
//                // test for reading
//                //Toast.makeText(mMainActivity, "Read success", Toast.LENGTH_SHORT).show();
//                int environmentActuatorSize = actuatorList.size();
//                tv_number_actuator_environment.setText(environmentActuatorSize + " Devices");
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(mMainActivity, "Error when reading data", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}
