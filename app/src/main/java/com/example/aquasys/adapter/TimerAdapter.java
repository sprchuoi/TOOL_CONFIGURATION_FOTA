//package com.example.aquasys.adapter;
//
//import android.annotation.SuppressLint;
//import android.app.AlertDialog;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CompoundButton;
//import android.widget.ImageView;
//import android.widget.NumberPicker;
//import android.widget.Switch;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.example.aquasys.MainActivity;
//import com.example.aquasys.R;
//import com.example.aquasys.listener.SelectListenerTimer;
//import com.example.aquasys.object.actuator;
//import com.example.aquasys.object.timer;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.List;
//
//public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.TimerViewHolder>{
//
//    private final List<timer> timerList;
//
//    public TimerAdapter( List<timer> timerList,SelectListenerTimer listenerTimer) {
//        this.timerList = timerList;
//    }
//
//    @NonNull
//    @Override
//    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_timer_schedule , parent,false);
//        return new TimerViewHolder(view);
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    @Override
//    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
//        timer tim = timerList.get(position);
//
//// Get the position of the item in the timer list
//        int currentPosition = holder.getAdapterPosition();
//
//// Null check for tim and holder
//        if (tim == null) {
//            return;
//        }
//        int startHour = tim.getTime_start_hour();
//        int startMinute = tim.getTime_start_minute();
//        String amPm;
//
//        if (startHour <= 12) {
//            amPm = "AM";
//            if (startHour == 0) {
//                startHour = 12; // Convert 0 AM to 12 AM
//            }
//        } else {
//            amPm = "PM";
//            startHour -= 12; // Convert 24-hour format to 12-hour format
//        }
//        holder.tv_name_timer_act.setText(tim.getAct().getName());
//        @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d %s",
//                startHour, startMinute ,amPm );
//        holder.tv_name_timer_act.setText(tim.getAct().getName());
//        holder.tv_timer_set.setText(time);
//
//
//// Set image for timer using Glide (assuming you have an image resource ID)
//
//
//        Glide.with(holder.itemView.getContext())
//                .load(tim.getAct().getImg())
//                .placeholder(R.drawable.unknown) // Add a placeholder image resource
//                .into(holder.img_actuator_timer);
//        holder.card_timer.setOnLongClickListener(v -> {
//            // Get adapter position of ViewHolder in RecyclerView and assign it to 'currentPosition'.
//            int itemPosition = holder.getAdapterPosition();
//
//            // Create a builder for an alert dialog that uses the default alert dialog theme.
//            new AlertDialog.Builder(v.getContext())
//                    .setTitle("Delete Schedule") // Set title text for dialog.
//                    .setMessage("Are you sure you want to delete this Schedule?") // Set message text for dialog.
//                    // Add positive button to dialog with text "OK" and click listener.
//                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
//                        // If 'currentPosition' is a valid position
//                        if (itemPosition != RecyclerView.NO_POSITION) {
//                            // Get the timer object to be removed from Firebase
//                            timer timerToRemove = timer.globalTimer.get(itemPosition);
//                            // Remove the item at 'itemPosition' from timerList.
//                            timerList.remove(itemPosition);
//                            notifyDataSetChanged();
//                            // Remove the timer with the current position in Firebase
//                            if (timerToRemove != null) {
//
//                                // set the flag is_schedule to false when delete actuator
//                                // Check the act in timer is water actuator or environment water
//                                if(timerToRemove.getAct().getType() == actuator.typeof_actuator.bulb){
//                                    for(int i = 0 ; i < actuator.globalActuator_environment.size() ; i++){
//                                        // With the water actuator
//                                        if(timerToRemove.getAct().getId().equals(actuator.globalActuator_environment.get(i).getId())) {
//                                            actuator.globalActuator_environment.get(i).setIs_schedule(false);
//                                            //push flag back to firebase when set the flag to false state
//                                            holder.mMainActivity.mDatabaseActuator_environment.child(String.valueOf(i)).child("is_schedule").setValue(actuator.globalActuator_environment.get(i).isIs_schedule());
//                                        }
//                                    }
//                                }
//                                else {
//                                    for(int j = 0 ; j < actuator.globalActuator_water.size() ; j++){
//                                        // With the environment actuator
//                                        if(timerToRemove.getAct().getId().equals(actuator.globalActuator_water.get(j).getId())) {
//                                            actuator.globalActuator_water.get(j).setIs_schedule(false);
//                                            //push flag back to firebase when set the flag to false state
//                                            holder.mMainActivity.mDatabaseActuator_water.child(String.valueOf(j)).child("is_schedule").setValue(actuator.globalActuator_water.get(j).isIs_schedule());
//                                        }
//                                    }
//                                }
//                                holder.mMainActivity.mDatabaseSchedule.child(String.valueOf(itemPosition)).removeValue()
//                                        .addOnSuccessListener(aVoid -> {
//                                          // Data has been removed successfully from Firebase
//                                            Toast.makeText(holder.mMainActivity, "Schedule deleted", Toast.LENGTH_SHORT).show();
//                                        })
//                                        .addOnFailureListener(e -> {
//                                            // Handle any errors
//                                            Toast.makeText(holder.mMainActivity, "Error deleting schedule", Toast.LENGTH_SHORT).show();
//                                        });
//                                for(int i  = itemPosition ; i < timerList.size() ; i++){
//                                    // save back to the firebase
//                                    holder.mMainActivity.mDatabaseSchedule.child(String.valueOf(i)).setValue(timerList.get(i));
//                                }
//                                if(itemPosition != timerList.size())
//                                    //remove the last item in firebase
//                                    holder.mMainActivity.mDatabaseSchedule.child(String.valueOf(timerList.size())).removeValue();
//
//                            }
//                        }
//                    })
//                    // Add negative button to dialog with text "Cancel" and null click listener.
//                    .setNegativeButton(android.R.string.cancel, null)
//                    // Set icon for dialog using a drawable resource.
//                    .setIcon(android.R.drawable.ic_menu_delete)
//                    // Show this dialog, adding it to the screen.
//                    .show();
//            return true;
//        });
//
//        // update the value error after delete
//        //Update realtime status schedule
//        holder.mMainActivity.mDatabaseSchedule.child(String.valueOf(holder.getAdapterPosition())).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    int status = snapshot.getValue(Integer.class);
//                    int currentPosition = holder.getAdapterPosition();
//                    // Verify that the tim object is not null
//                    if (tim != null) {
//                        int offTimerColor = ContextCompat.getColor(holder.mMainActivity, R.color.card_timer_disable);
//                        int onTextTimerColor = ContextCompat.getColor(holder.mMainActivity, R.color.black);
//
//                        if (status == 1) {
//                           holder.btn_timer_on_off.setChecked(true);
//                           holder.tv_timer_set.setTextColor(onTextTimerColor);
//                           holder.tv_name_timer_act.setTextColor(onTextTimerColor);
//
//                        } else if (status == 0) {
//                            holder.btn_timer_on_off.setChecked(false);
//                            holder.tv_timer_set.setTextColor(offTimerColor);
//                            holder.tv_name_timer_act.setTextColor(offTimerColor);
//                        }
//                    }
//                } else {
//                    // Handle the case when the status data is not found
//                    // You may want to set some default values or take appropriate action
//                    // For example, setting the default status to 0 and updating the UI accordingly
//                    holder.btn_timer_on_off.setChecked(false);
//                    tim.setStatus(0);
//                    int offTimerColor = ContextCompat.getColor(holder.mMainActivity, R.color.card_timer_disable);
//                    holder.card_timer.setBackgroundColor(offTimerColor);
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(holder.mMainActivity, "Error when reading data", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//        holder.card_timer.setOnClickListener(v -> {
//            // get current position
//            AlertDialog.Builder builder = new AlertDialog.Builder(holder.mMainActivity);
//            View  diaLogView = holder.mMainActivity.getLayoutInflater().inflate(R.layout.dialog_edit_timer, null);
//            builder.setView(diaLogView);
//            builder.setTitle("Edit Schedule(hour:minute)");
//            builder.setIcon(R.drawable.edit);
//            // mapping for num picker in layout edit schedule
//            NumberPicker np_edit_duration_hour_from = diaLogView.findViewById(R.id.np_edit_duration_hour_from);
//            NumberPicker np_edit_duration_minute_from = diaLogView.findViewById(R.id.np_edit_duration_minute_from);
//            NumberPicker np_edit_duration_am_pm = diaLogView.findViewById(R.id.np_edit_duration_am_pm);
//            // set threshold for num picker
//            np_edit_duration_hour_from.setMaxValue(12);
//            np_edit_duration_hour_from.setMinValue(0);
//            np_edit_duration_minute_from.setMaxValue(59);
//            np_edit_duration_minute_from.setMinValue(0);
//            //set AM and PM for time picker
//            String[] displayedValues = {"AM", "PM"};
//            np_edit_duration_am_pm.setMinValue(0);
//            np_edit_duration_am_pm.setMaxValue(displayedValues.length - 1);
//            np_edit_duration_am_pm.setDisplayedValues(displayedValues);
//            // set time initiate for num picker when
//
//            if(timerList.get(currentPosition).getTime_start_hour() < 12){
//                np_edit_duration_hour_from.setValue(timerList.get(currentPosition).getTime_start_hour());
//                np_edit_duration_am_pm.setValue(0);
//            }
//
//            else {
//                np_edit_duration_hour_from.setValue(timerList.get(currentPosition).getTime_start_hour() - 12);
//                np_edit_duration_am_pm.setValue(1);
//            }
//
//            np_edit_duration_minute_from.setValue(timerList.get(currentPosition).getTime_start_minute());
//            builder.setPositiveButton("Confirm", (dialog, which) -> {
//                int hourFrom = 0 ;
//                if(np_edit_duration_am_pm.getValue() == 0 ) {
//                    hourFrom = np_edit_duration_hour_from.getValue();
//                }
//                else hourFrom = np_edit_duration_hour_from.getValue() + 12 ;
//                int minuteFrom = np_edit_duration_minute_from.getValue();
//                // change the schedule time
//                timerList.get(currentPosition).setTime_start_hour(hourFrom);
//                timerList.get(currentPosition).setTime_start_minute(minuteFrom);
//                notifyDataSetChanged();
//                //push timer to firebase after change the setting time of timer
//                holder.mMainActivity.mDatabaseSchedule.child(String.valueOf(currentPosition)).setValue(timerList.get(currentPosition));
//            });
//            builder.setNegativeButton("Cancel" ,null);
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        });
//
//        // setting enable for the button
//
//        holder.btn_timer_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                int adapterPosition = holder.getAdapterPosition();
//
//                // Check if the adapter position is valid
//                if (adapterPosition != RecyclerView.NO_POSITION) {
//                    int color;
//
//                    int onTextTimerColor = ContextCompat.getColor(holder.mMainActivity, R.color.black);
//
//                    if (isChecked) {
//                        // Change the color of the card timer
//                        color = ContextCompat.getColor(holder.mMainActivity, R.color.card_timer_enable);
//                        holder.card_timer.setBackgroundColor(color);
//                        holder.tv_timer_set.setTextColor(onTextTimerColor);
//                        holder.tv_name_timer_act.setTextColor(onTextTimerColor);
//                        // Change the status of the item in the list
//                        timerList.get(adapterPosition).setStatus(1);
//                    } else {
//                        // Change the color of the card timer
//                        color = ContextCompat.getColor(holder.mMainActivity, R.color.card_timer_disable);
//                        //holder.card_timer.setBackgroundColor(color);
//                        holder.tv_timer_set.setTextColor(color);
//                        holder.tv_name_timer_act.setTextColor(color);
//                        // Change the status of the item in the list
//                        timerList.get(adapterPosition).setStatus(0);
//                    }
//                    //set value for turn on of button
//                    holder.mMainActivity.mDatabaseSchedule.child(String.valueOf(currentPosition)).child("status").setValue(timerList.get(adapterPosition).getStatus());
//
//                }
//            }
//        });
//
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return timerList.size();
//    }
//    public static class TimerViewHolder extends RecyclerView.ViewHolder{
//
//        private final TextView tv_name_timer_act;
//        private final TextView tv_timer_set;
//        private final MainActivity mMainActivity;
//        @SuppressLint("UseSwitchCompatOrMaterialCode")
//        private final  Switch btn_timer_on_off;
//
//        private final ConstraintLayout card_timer;
//        private final ImageView img_actuator_timer;
//        public TimerViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tv_name_timer_act = itemView.findViewById(R.id.tv_name_timer_act);
//            tv_timer_set = itemView.findViewById(R.id.tv_timer_set);
//            btn_timer_on_off = itemView.findViewById(R.id.btn_timer_on_off);
//            card_timer = itemView.findViewById(R.id.card_timer);
//            img_actuator_timer = itemView.findViewById(R.id.img_actuator_timer);
//            mMainActivity = (MainActivity) itemView.getContext();
//        }
//    }
//
//}
