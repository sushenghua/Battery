package com.miirr.shenghua.batterylog;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by shenghua on 11/14/15.
 */
public class StatusFragment extends Fragment {

    private TextView statusView;
    private TextView levelView;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static StatusFragment newInstance() {
        StatusFragment fragment = new StatusFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return fragment;
    }

    public StatusFragment() {
    }

    public void setStatusText(String text) {
        statusView.setText(text);
    }

    public void setLevelText(String text) {
        levelView.setText(text);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.status, container, false);

        statusView = (TextView)rootView.findViewById(R.id.batteryStatusTextView);
        levelView = (TextView)rootView.findViewById(R.id.batteryLevelView);

        TextView cpuInfoView = (TextView)rootView.findViewById(R.id.cpuInfoView);
        TextView manufacturerInfoView = (TextView)rootView.findViewById(R.id.manufacturerInfoView);
        TextView modelInfoView = (TextView)rootView.findViewById(R.id.modelInfoView);
        TextView buildInfoView = (TextView)rootView.findViewById(R.id.buildInfoView);

        statusView.setTextColor(Color.BLUE);
        levelView.setTextColor(Color.BLUE);

        cpuInfoView.setTextColor(Color.BLUE);
        manufacturerInfoView.setTextColor(Color.BLUE);
        modelInfoView.setTextColor(Color.BLUE);
        buildInfoView.setTextColor(Color.BLUE);

        cpuInfoView.setText(Build.HARDWARE);
        manufacturerInfoView.setText(Build.MANUFACTURER);
        modelInfoView.setText(Build.MODEL);
        buildInfoView.setText(Build.ID);

        return rootView;
    }
}
