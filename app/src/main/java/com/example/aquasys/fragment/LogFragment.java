//package com.example.aquasys.fragment;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.anychart.AnyChart;
//import com.anychart.AnyChartView;
//import com.anychart.chart.common.dataentry.DataEntry;
//import com.anychart.chart.common.dataentry.ValueDataEntry;
//import com.anychart.charts.Cartesian;
//import com.anychart.charts.Pie;
//import com.anychart.core.annotations.Line;
//import com.anychart.data.Mapping;
//import com.anychart.data.Set;
//import com.anychart.enums.Anchor;
//import com.anychart.enums.MarkerType;
//import com.anychart.enums.TooltipPositionMode;
//import com.anychart.graphics.vector.Stroke;
//import com.example.aquasys.MainActivity;
//import com.example.aquasys.R;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//
//public class LogFragment extends Fragment {
//
//    private TextView tv_log_content;
//
//    private MainActivity mMainActivity; // A reference to the main activity.
//
//
//
//    public LogFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View mView = inflater.inflate(R.layout.fragment_log, container, false); // Inflate the fragment_home layout into mView.
//        mMainActivity = (MainActivity) getActivity(); // Get a reference to the hosting Activity (assumed to be MainActivity).
//        tv_log_content = mView.findViewById(R.id.tv_log_content);
//        tv_log_content.setText(mMainActivity.loadAndDisplayLog());
//        // Load log content and display it
//        return mView;
//    }
//
//
//}