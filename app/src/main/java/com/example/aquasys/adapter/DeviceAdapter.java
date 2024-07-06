package com.example.aquasys.adapter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aquasys.MainActivity;
import com.example.aquasys.R;
import com.example.aquasys.object.nodeSensor;

import java.util.List;
import java.util.Objects;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private final Context context;
    private final List<nodeSensor> nodeSensorList;
    private String updatingNodeId; // Thêm trường này
    public  int Version_upcoming_main;
    public int Version_upcoming_sub;
    public String time_startOTA;
    public String time_finishOTA;
    public String Duration_OTA;
    public String Status_Update;
    MainActivity mMainActivity ;

    public DeviceAdapter(Context context, List<nodeSensor> nodeSensorList) {
        this.context = context;
        this.nodeSensorList = nodeSensorList;
        this.updatingNodeId = ""; // Khởi tạo giá trị ban đầu
    }

    // Thêm setter cho UpdateButtonClickListener
    public void setUpdateButtonClickListener(UpdateButtonClickListener listener) {
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_device_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        nodeSensor nodeSensor = nodeSensorList.get(position);
        holder.deviceIdTextView.setText("Node ID: " + nodeSensor.getAddressSensor());
        holder.deviceVersionTextView.setText("Version: " + nodeSensor.getApplVersion_main() + "." + nodeSensor.getApplVersion_sub());

        // Kiểm tra và cập nhật trạng thái của nút
        if (nodeSensor.getAddressSensor().equals(updatingNodeId)) {
            holder.updateButton.setEnabled(true);
            holder.progressBar.setVisibility(View.GONE);
            holder.updateInfoTextView.setVisibility(View.GONE);
            holder.updateButton.setEnabled(true);
            // Check if the upcoming version is the same as the current version
            if (nodeSensor.getApplVersion_main() == Version_upcoming_main &&
                    nodeSensor.getApplVersion_sub() == Version_upcoming_sub) {
                holder.deviceVersionTextView.setText("Version: " + nodeSensor.getApplVersion_main() + "." + nodeSensor.getApplVersion_sub() + " (Latest)");
                holder.updateButton.setText("Latest");
                holder.updateButton.setEnabled(false);
            } else {
                holder.deviceVersionTextView.setText("Version: " + nodeSensor.getApplVersion_main() + "." + nodeSensor.getApplVersion_sub() + " -> "
                        + Version_upcoming_main + "." + Version_upcoming_sub);
                holder.updateButton.setText("Update");
            }
        } else {
            holder.updateButton.setEnabled(false);
            holder.progressBar.setVisibility(View.GONE);
            holder.updateInfoTextView.setVisibility(View.GONE);
            holder.updateInfoTextView.setText("Downloading");
        }
        // Kiểm tra trạng thái cập nhật và hiển thị ProgressBar và TextView tương ứng
        if (Objects.equals(nodeSensor.getStatus_update(), "true")) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.updateInfoTextView.setVisibility(View.VISIBLE);
        }
        else if(Objects.equals(nodeSensor.getStatus_update(), "updating")){
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.updateInfoTextView.setVisibility(View.VISIBLE);
            holder.updateInfoTextView.setText("Updating");
            holder.Time_estimate.setVisibility(View.VISIBLE);
            holder.Time_estimate.setText("Time Start: "+ time_startOTA);
        } else if (Objects.equals(nodeSensor.getStatus_update(), "fail")) {
            holder.updateButton.setText("Retry");
            //holder.updateButton.setBackgroundColor(R.color.red);
            holder.deviceVersionTextView.setText("Version: " + nodeSensor.getApplVersion_main() + "." + nodeSensor.getApplVersion_sub() + " -> "
                    + Version_upcoming_main + "." + Version_upcoming_sub);
            holder.updateButton.setEnabled(true);
        }
        else if(Objects.equals(nodeSensor.getStatus_update(), "done")){
            holder.updateButton.setEnabled(false);
            holder.progressBar.setVisibility(View.GONE);
            holder.updateInfoTextView.setVisibility(View.GONE);
            holder.updateInfoTextView.setText("Install Done");
            nodeSensor.setStatus_update("done");
            nodeSensor.setApplVersion_main(Version_upcoming_main);
            nodeSensor.setApplVersion_sub(Version_upcoming_sub);
            nodeSensor.setOTA_Duration(Duration_OTA);
            nodeSensor.setTime_OTA(time_finishOTA);
            holder.Time_estimate.setVisibility(View.VISIBLE);
            holder.Time_estimate.setText("Time Start: "+ time_startOTA);
            holder.updateButton.setText("Latest");
            holder.tv_time_OTA_duration.setVisibility(View.VISIBLE);
            holder.tv_time_OTA_duration.setText("Duration: " + Duration_OTA);
            //update version
            holder.mMainActivity.UpdateVersionFirebase(updatingNodeId , Version_upcoming_main , Version_upcoming_sub);
            holder.mMainActivity.UpDateStatus_updateToFireBase_False();
            // Sync up version
            //holder.mMainActivity.addNodeSensorToFireBase();
        }

        // Sync up version
        //holder.mMainActivity.addNodeSensorToFireBase();

        holder.updateButton.setOnClickListener(v -> {
            holder.mMainActivity.UpDateStatus_updateToFireBase(); // Truyền nodeSensor khi bấm nút cập nhật
        });
    }

    @Override
    public int getItemCount() {
        return nodeSensorList.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;
        TextView deviceIdTextView;
        TextView deviceVersionTextView;
        Button updateButton;
        TextView updateInfoTextView;
        TextView Time_estimate;
        TextView tv_time_OTA_duration;
        MainActivity mMainActivity;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceIdTextView = itemView.findViewById(R.id.tv_device_id);
            deviceVersionTextView = itemView.findViewById(R.id.tv_device_version);
            updateButton = itemView.findViewById(R.id.btn_update_device);
            progressBar = itemView.findViewById(R.id.progressBar);
            updateInfoTextView = itemView.findViewById(R.id.updateInfoTextView);
            mMainActivity = (MainActivity) itemView.getContext();
            Time_estimate = itemView.findViewById(R.id.tv_time_status);
            tv_time_OTA_duration = itemView.findViewById(R.id.tv_time_OTA_duration);
        }
    }

    public interface UpdateButtonClickListener {
        void onUpdateButtonClick(nodeSensor nodeSensor);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDeviceInfo(String nodeId, String statusUpdate, int version_main , int version_sub ,String duration_OTA, String time_StartOTA , String timeFinishOTA) {
        for (nodeSensor node : nodeSensorList) {
            if (node.getAddressSensor().equals(nodeId)) {
                Version_upcoming_main  = version_main;
                Version_upcoming_sub = version_sub;
                time_startOTA = time_StartOTA;
                time_finishOTA = timeFinishOTA;
                Duration_OTA = duration_OTA;
                Status_Update = statusUpdate;
                //node.setApplVersion(version);
                if (Objects.equals(statusUpdate, "false")) {
                    updatingNodeId = nodeId; // Cập nhật nodeId đang cập nhật
                } else if (updatingNodeId.equals(nodeId)) {
                    updatingNodeId = ""; // Reset nếu cập nhật hoàn thành
                }
                node.setStatus_update(statusUpdate);
                notifyDataSetChanged(); // Cập nhật lại UI
                break;
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void hideProgressBarForNode(String nodeId) {
        for (nodeSensor node : nodeSensorList) {
            if (node.getAddressSensor().equals(nodeId)) {
                node.setStatus_update("true");
                if (updatingNodeId.equals(nodeId)) {
                    updatingNodeId = ""; // Reset nếu cập nhật hoàn thành
                }
                notifyDataSetChanged(); // Cập nhật lại UI
                break;
            }
        }
    }

}
