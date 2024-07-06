//package com.example.aquasys.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CompoundButton;
//import android.widget.TextView;
//import android.widget.ToggleButton;
//
//import androidx.annotation.NonNull;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.aquasys.MainActivity;
//import com.example.aquasys.R;
//
//import com.example.aquasys.object.actuator;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TimerActuatorAdapter extends RecyclerView.Adapter<TimerActuatorAdapter.TimerActuatorViewHolder>{
//
//    private final List<actuator> actuatorList;
//
//
//    public TimerActuatorAdapter(List<actuator> actuatorList) {
//        this.actuatorList = actuatorList;
//
//    }
//
//    @NonNull
//    @Override
//    public TimerActuatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeractuator_layout, parent, false);
//        return new TimerActuatorViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull TimerActuatorViewHolder holder, int position) {
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
//
//
//        }
//        holder.tv_timeractuator.setText(act.getName());
//
//        holder.btn_actuator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                actuator.globalActuator_timer = null;
//                int itemPosition = holder.getAdapterPosition();
//                actuator act = actuatorList.get(itemPosition);
//                // Add the select list of actuator to the timer actuator for create new timer
//                if (isChecked) {
//                    if (actuator.globalActuator_timer == null) {
//                        actuator.globalActuator_timer = new ArrayList<>();
//                    }
//                    actuator.globalActuator_timer.add(act);
//                } else {
//                    if (actuator.globalActuator_timer != null) {
//                        actuator.globalActuator_timer.remove(act);
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
//    public static class TimerActuatorViewHolder extends RecyclerView.ViewHolder {
//        private final ToggleButton btn_actuator;
//        private final TextView tv_timeractuator;
//        MainActivity mMainactivity;
//
//        public TimerActuatorViewHolder(@NonNull View itemView) {
//            super(itemView);
//            btn_actuator = itemView.findViewById(R.id.btn_actuator);
//            mMainactivity = (MainActivity) itemView.getContext();
//            tv_timeractuator = itemView.findViewById(R.id.tv_timeractuator);
//        }
//    }
//}
