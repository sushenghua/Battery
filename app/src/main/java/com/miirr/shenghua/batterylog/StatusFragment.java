package com.miirr.shenghua.batterylog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by shenghua on 11/14/15.
 */
public class StatusFragment extends Fragment {

    private TextView statusView = null;
    private TextView levelView = null;

    // broadcast receiver
    private BroadcastReceiver batteryStatusReceiver;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBatteryStatusReceiver();
        Log.d("StatusFragment", "------------>onCreate");
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


        Log.d("StatusFragment", "------------>onCreateView");

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBatteryStatusReceiver();
        Log.d("StatusFragment", "------------>onResume");

    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d("StatusFragment", "------------>onPause");
    }
    @Override
    public void onStop() {
        super.onStop();
        unregisterBatteryStatusReceiver();
        Log.d("StatusFragment", "------------>onStop");
    }

//    public void onDestoryView() {
//        statusView = null;
//        levelView = null;
//        super.onDestroyView();
//    }

//    public void onDestroy() {
//        Log.d("StatusFragment", "------------>onDestroy");
//        unregisterBatteryStatusReceiver();
//        super.onDestroy();
//    }

    public void updateStatus(String text) {
        Log.d("StatusFragment", "====>updateStatus: "+text);
        if (statusView != null)
            statusView.setText(text);
        else {
            Log.d("StatusFragment", "====>updateStatus: statusView null");
        }
    }

    public void updateLevel(String text) {
        Log.d("StatusFragment", "====>updateLevel: "+text);
        if (levelView != null)
            levelView.setText(text);
    }

    public void unregisterBatteryStatusReceiver() {
        if (batteryStatusReceiver != null) {
            getActivity().unregisterReceiver(batteryStatusReceiver);
        }
    }

    private void refreshBatteryView() {

    }

    public void registerBatteryStatusReceiver() {
        // inspect battery charge change
        batteryStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {

                    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;
                    if (isCharging) {
                        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                        String chargeType = "";
                        if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) {
                            chargeType = "(USB)";
                        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
                            chargeType = "(AC)";
                        }
//                        statusView.setText("Charging" + chargeType);
                        updateStatus("Charging" + chargeType);
                    } else {
//                        statusView.setText("Not Charged");
                        updateStatus("Not Charged");
                    }

                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//                    levelView.setText((int) ((level / (float) scale) * 100) + "%");
                    updateLevel( (int)((level / (float) scale) * 100) + "%" );
                    Log.d("batteryStatusReceiver", "******>level: " + level + ", scale: " + scale);
                }
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(batteryStatusReceiver, filter);
    }
}
