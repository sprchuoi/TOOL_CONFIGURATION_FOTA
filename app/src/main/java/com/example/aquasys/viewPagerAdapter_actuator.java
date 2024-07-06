//package com.example.aquasys;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.viewpager2.adapter.FragmentStateAdapter;
//
//
//public class viewPagerAdapter_actuator extends FragmentStateAdapter {
//    public viewPagerAdapter_actuator(@NonNull Fragment fragment) {
//        super(fragment);
//    }
//
//    @NonNull
//    @Override
//    public Fragment createFragment(int position) {
//        switch (position){
//            case 0 :
//                return new fragment_actuator_environment();
//            case 1 :
//                return new fragment_actuator_water();
//            default:
//                return new fragment_actuator_environment();
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return 2;
//    }
//}
