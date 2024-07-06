package com.example.aquasys;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


import com.example.aquasys.fragment.SensorFragment;
import com.example.aquasys.fragment.UpdateManager;

public class viewPagerAdapter extends FragmentStateAdapter {
    public viewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0 :
                return new SensorFragment();
            case 1:
                return new UpdateManager();
            default:
                return new SensorFragment();
        }


    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
