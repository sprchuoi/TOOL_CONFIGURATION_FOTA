package com.example.aquasys.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aquasys.MainActivity;
import com.example.aquasys.R;
import com.example.aquasys.object.nodeSensor;

import java.util.List;

public class NodeSensorAdapter extends RecyclerView.Adapter<NodeSensorAdapter.NodeSensorViewHolder> {

    private final List<nodeSensor> nodeSensorList;
    private final Context context;
    MainActivity mMainActivity;

    public NodeSensorAdapter(Context context, List<nodeSensor> nodeSensorList) {
        this.context = context;
        this.nodeSensorList = nodeSensorList;
    }

    @NonNull
    @Override
    public NodeSensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_node_sensor, parent, false);
        return new NodeSensorViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull NodeSensorViewHolder holder, int position) {
        nodeSensor nodeSensor = nodeSensorList.get(position);
        holder.tvNodeId.setText("NODE ID: "+nodeSensor.getAddressSensor());
        holder.tvNodeVersion.setText("Version: "+nodeSensor.getApplVersion_main()+"."+nodeSensor.getApplVersion_sub());
        holder.tvTimeUpdate.setText("Time Update:"+holder.mMainActivity.formatFirebaseDateTime(nodeSensor.getTime_Update()));
        mMainActivity = holder.mMainActivity;
        // Setup the nested RecyclerView
        SensorAdapter sensorAdapter = new SensorAdapter(nodeSensor.getListSensor());
        sensorAdapter.context = context; // Pass context to SensorAdapter
        // Update device status
        String status = nodeSensor.getStatus_Node(); // Assuming you have a getStatus method
        holder.txDeviceStatus.setText(status.equals("on") ? "Active" : "Off");
        holder.txDeviceStatus.setTextColor(status.equals("on") ?
                context.getResources().getColor(android.R.color.holo_green_dark) :
                context.getResources().getColor(android.R.color.darker_gray));
        holder.recyclerViewSensorList.setLayoutManager(new GridLayoutManager(holder.recyclerViewSensorList.getContext(), 2));
        holder.recyclerViewSensorList.setAdapter(sensorAdapter);

    }

    @Override
    public int getItemCount() {
        return nodeSensorList.size();
    }

    static class NodeSensorViewHolder extends RecyclerView.ViewHolder {
        TextView tvNodeId, tvNodeVersion , tvTimeUpdate ,txDeviceStatus;
        RecyclerView recyclerViewSensorList;
        MainActivity mMainActivity;

        public NodeSensorViewHolder(@NonNull View itemView) {
            super(itemView);
            mMainActivity = (MainActivity) itemView.getContext();
            tvNodeId = itemView.findViewById(R.id.tv_node_id);
            tvNodeVersion = itemView.findViewById(R.id.tv_node_version);
            recyclerViewSensorList = itemView.findViewById(R.id.recyclerview_sensor_list);
            tvTimeUpdate = itemView.findViewById(R.id.tv_time_update);
            txDeviceStatus = itemView.findViewById(R.id.tv_device_status);
        }
    }


}
