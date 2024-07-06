//package com.example.aquasys.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CompoundButton;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.ToggleButton;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.aquasys.MainActivity;
//import com.example.aquasys.R;
//import com.example.aquasys.object.actuator;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ActuatorAdapter_add extends RecyclerView.Adapter<ActuatorAdapter_add.ActuatorViewHolder> {
//
//    private final List<actuator> actuatorList;
//
//    public ActuatorAdapter_add(List<actuator> actuatorList) {
//        this.actuatorList = actuatorList;
//    }
//
//    @NonNull
//    @Override
//    public ActuatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.addactuator_layout, parent, false);
//        return new ActuatorViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ActuatorViewHolder holder, int position) {
//        actuator act = actuatorList.get(position);
//        if(act == null)
//            return;
//        //set drawable effect for btn
//        switch (act.getType()) {
//            case bulb:
//                holder.btn_actuator.setBackgroundResource(R.drawable.btn_bulb1);
//                break;
//            case pump:
//                holder.btn_actuator.setBackgroundResource(R.drawable.btn_pump);
//                break;
//            case heater:
//                holder.btn_actuator.setBackgroundResource(R.drawable.btn_heater);
//                break;
//            case feeder:
//                holder.btn_actuator.setBackgroundResource(R.drawable.btn_feeder);
//                break;
//            default: break;
//
//        }
//        //holder.tv_timeractuator.setText(act.getName());
//
//        holder.btn_actuator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                actuator.globalActuator_timer = null;
//                int itemPosition = holder.getAdapterPosition();
//                actuator act = actuatorList.get(itemPosition);
//
//                if (isChecked) {
//                    if (actuator.globalActuator_add == null) {
//                        actuator.globalActuator_add = new ArrayList<>();
//                    }
//                    if (actuator.globalActuator_edit == null) {
//                    }
//                    actuator.globalActuator_edit = new ArrayList<>();
//                    actuator.globalActuator_add.add(act);
//                    actuator.globalActuator_edit.add(act);
//                    holder.mMainactivity.pos_edit_actuator = itemPosition;
//                } else {
//                    if (actuator.globalActuator_add != null) {
//                        actuator.globalActuator_add.remove(act);
//                    }
//                }
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return actuatorList.size();
//    }
//
//    public static class ActuatorViewHolder extends RecyclerView.ViewHolder{
//
//        private final ToggleButton btn_actuator;
//        private final TextView tv_timeractuator;
//        MainActivity mMainactivity;
//        public ActuatorViewHolder(@NonNull View itemView) {
//            super(itemView);
//            btn_actuator = itemView.findViewById(R.id.btn_actuator);
//            mMainactivity = (MainActivity) itemView.getContext();
//            tv_timeractuator = itemView.findViewById(R.id.tv_timeractuator);
//        }
//    }
//}
