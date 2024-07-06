package com.example.aquasys.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aquasys.MainActivity;
import com.example.aquasys.R;
import com.example.aquasys.listener.SelectListener;
import com.example.aquasys.object.sensor;

import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {

    Context context;
    private final List<sensor> mListSensor;
    private final String[] sensorTypes = {"Humidity", "CO2", "Temperature", "Water level", "Moisture Soil" , "Spo2" , "Light" ,"Gas"};

    // Constructor for the SensorAdapter class
    public SensorAdapter(List<sensor> mListSensor) {
        this.mListSensor = mListSensor;
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sensor, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        sensor sen = mListSensor.get(position);
        if (sen == null) {
            return;
        }
        holder.tvSensor.setText(sen.getName());

        String sensorValue = sen.getValue();
        if("1".equals(sensorValue)){
            sensorValue = "Not Use";
        } else if ("-1".equals(sensorValue)) {
            sensorValue = "Not Use";
        } else{
            switch (sen.getName()) {
                case "Humidity":
                case "Moisture Soil":
                    sensorValue = sensorValue + "%";
                    break;
                case "Temperature":
                    sensorValue = sensorValue + "°C";
                    break;
                case "Water level":
                    sensorValue = sensorValue + "cm";
                    break;
            }
        }


        holder.tvSensorVal.setText(sensorValue);
        holder.imgSensor.setImageResource(sen.getImg());

        holder.layoutSensor.setOnClickListener(v -> showSensorTypeDialog(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return mListSensor.size();
    }

    // Create sensor view holder
    public static class SensorViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSensor;
        private final TextView tvSensorVal;
        private final ImageView imgSensor;
        LinearLayout layoutSensor;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSensor = itemView.findViewById(R.id.tv_sensor);
            tvSensorVal = itemView.findViewById(R.id.tv_sensor_val);
            imgSensor = itemView.findViewById(R.id.img_sensor);
            layoutSensor = itemView.findViewById(R.id.layout_sensor);
        }
    }

    private void showSensorTypeDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Sensor Type");
        builder.setItems(sensorTypes, (dialog, which) -> {
            sensor sensor = mListSensor.get(position);
            sensor.setName(sensorTypes[which]);
            sensor.setImg(sensor.getImageResource(sensorTypes[which])); // Update the image
            notifyItemChanged(position);
        });
        builder.show();
    }

}
