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

    private static final String TAG = "StatusFragment";

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
        //registerBatteryStatusReceiver();
//        Log.d(TAG, "------------>onCreate");
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

//        Log.d(TAG, "------------>onCreateView");
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBatteryStatusReceiver();
        //Log.d(TAG, "------------>onResume");

    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterBatteryStatusReceiver();
        //Log.d(TAG, "------------>onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterBatteryStatusReceiver();
        //Log.d(TAG, "------------>onStop");
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
//        Log.d("StatusFragment", "====>updateStatus: "+text);
        if (statusView != null)
            statusView.setText(text);
        else {
//            Log.d(TAG, "====>updateStatus: statusView null");
        }
    }

    public void updateLevel(String text) {
//        Log.d(TAG, "====>updateLevel: "+text);
        if (levelView != null)
            levelView.setText(text);
    }

    public void unregisterBatteryStatusReceiver() {
        if (batteryStatusReceiver != null) {
            getActivity().unregisterReceiver(batteryStatusReceiver);
            batteryStatusReceiver = null;
        }
    }

    public void registerBatteryStatusReceiver() {
        // inspect battery charge change
        batteryStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BatteryLogService.ACTION_BATTERYSTATUS_CHANGED)) {
                    updateFromService();
                }
            }
        };

        IntentFilter filter = new IntentFilter(BatteryLogService.ACTION_BATTERYSTATUS_CHANGED);
        getActivity().registerReceiver(batteryStatusReceiver, filter);
        updateFromService();
    }

    private void updateFromService() {
        if (BatteryLogService.getChargeType() == BatteryLogService.BATTERY_AC_CHARGE) {
            updateStatus("Charging(AC)");
        }
        else if (BatteryLogService.getChargeType() == BatteryLogService.BATTERY_USB_CHARGE) {
            updateStatus("Charging(USB)");
        }
        else {
            updateStatus("No Charge");
        }

        updateLevel(BatteryLogService.getCurrentLevel() + "%");
//        Log.d(TAG, "updateFromService() level: "+BatteryLogService.getCurrentLevel());
    }
}
