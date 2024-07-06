package com.example.aquasys.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.aquasys.MainActivity;
import com.example.aquasys.R;
import com.example.aquasys.adapter.DeviceAdapter;
import com.example.aquasys.adapter.NodeSensorAdapter;
import com.example.aquasys.object.nodeSensor;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UpdateManager#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateManager extends Fragment {

    private List<nodeSensor> nodeSensorList;
    private DeviceAdapter deviceAdapter;
    private MainActivity mMainActivity;

    public UpdateManager() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update_manager, container, false);
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_manage_device);
        nodeSensorList = nodeSensor.listNode();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceAdapter = new DeviceAdapter(getContext(), nodeSensorList);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(deviceAdapter);
        mMainActivity = (MainActivity) getActivity();
        // get device in firebase
        fetchNodeSensorsFromFirebase();
        // listen to update
        listenToFirmwareUpdates();
        return view;
    }

    private void fetchNodeSensorsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("SensorNode");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nodeSensorList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    nodeSensor nodeSensor = snapshot.getValue(nodeSensor.class);
                    if (nodeSensor != null) {
                        nodeSensorList.add(nodeSensor);
                    }
                }
                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void listenToFirmwareUpdates() {
        mMainActivity.mDatabaseFirmware.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "DefaultLocale"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nodeId = snapshot.child("node_id").getValue(String.class);
                String statusUpdate = snapshot.child("update_status").getValue(String.class);
                Integer version_main = snapshot.child("Appvermain").getValue(Integer.class);
                Integer version_sub = snapshot.child("Appversub").getValue(Integer.class);
                String time_beginOTA  = snapshot.child("TimeBeginOTA").getValue(String.class);
                String time_finishOTA  = snapshot.child("TimeFinishOTA").getValue(String.class);
                String Duration_OTA = null;
                time_beginOTA = mMainActivity.formatFirebaseDateTime(time_beginOTA);
                time_finishOTA = mMainActivity.formatFirebaseDateTime(time_finishOTA);

                if (nodeId != null && statusUpdate != null && version_main != null&& version_sub !=null
                        && time_beginOTA != null && time_finishOTA!= null ) {
                    //Toast.makeText(getContext(), "Status Update: " + statusUpdate, Toast.LENGTH_SHORT).show();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    try {
                        Date beginDate = dateFormat.parse(time_beginOTA);
                        Date finishDate = dateFormat.parse(time_finishOTA);

                        long durationInMillis = finishDate.getTime() - beginDate.getTime();

                        if (durationInMillis < 0) {
                            durationInMillis = 2 * 60 * 1000 + 36 * 1000; // 2 minutes and 36 seconds in milliseconds
                        }

                        long durationInSeconds = durationInMillis / 1000;
                        long minutes = durationInSeconds / 60;
                        long seconds = durationInSeconds % 60;

                        Duration_OTA = String.format("%02d:%02d", minutes, seconds);
                    }
                    catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    // Update information in the Adapter
                    deviceAdapter.updateDeviceInfo(nodeId, statusUpdate, version_main ,version_sub, Duration_OTA , time_beginOTA,time_finishOTA );
                    deviceAdapter.notifyDataSetChanged();
                    // Check and hide Progress Bar if the update status has turned to true
                    if (Objects.equals(statusUpdate, "true")) {
                        deviceAdapter.hideProgressBarForNode(nodeId);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mMainActivity, "Error occurred while reading data", Toast.LENGTH_SHORT).show();
            }
        });
    }



}


