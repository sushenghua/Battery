package com.miirr.shenghua.batterylog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by shenghua on 11/14/15.
 */
public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";

    private TextView statusView = null;
    private TextView levelView = null;

    private TextView temperatureValue = null;
    private TextView voltageValue = null;
    private TextView healthValue = null;
    private TextView speedyChargeValue = null;

    private int chargeType = BatteryLogService.BATTERY_NO_CHARGE;
    private ImageView[] chargeTypeIcons = null;
    private BatteryView batteryView = null;

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
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);

//        statusView = (TextView)rootView.findViewById(R.id.batteryStatusTextView);
//        levelView = (TextView)rootView.findViewById(R.id.batteryLevelView);
//
//        TextView cpuInfoView = (TextView)rootView.findViewById(R.id.cpuInfoView);
//        TextView manufacturerInfoView = (TextView)rootView.findViewById(R.id.manufacturerInfoView);
//        TextView modelInfoView = (TextView)rootView.findViewById(R.id.modelInfoView);
//        TextView buildInfoView = (TextView)rootView.findViewById(R.id.buildInfoView);
//
//        statusView.setTextColor(Color.BLUE);
//        levelView.setTextColor(Color.BLUE);
//
//        cpuInfoView.setTextColor(Color.BLUE);
//        manufacturerInfoView.setTextColor(Color.BLUE);
//        modelInfoView.setTextColor(Color.BLUE);
//        buildInfoView.setTextColor(Color.BLUE);
//
//        cpuInfoView.setText(Build.HARDWARE);
//        manufacturerInfoView.setText(Build.MANUFACTURER);
//        modelInfoView.setText(Build.MODEL);
//        buildInfoView.setText(Build.ID);

//        Log.d(TAG, "------------>onCreateView");

        initHardwareInfoView(rootView);
        initChargeStatusView(rootView);
        initBatteryView(rootView);

        rootView.findViewById(R.id.battery_usage_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBatteryUsageActivity();
            }
        });

        return rootView;
    }

    private void startBatteryUsageActivity() {
        Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
        ResolveInfo resolveInfo = getContext().getPackageManager().resolveActivity(powerUsageIntent, 0);
        if(resolveInfo != null){
            startActivity(powerUsageIntent);
        } else {
            Toast.makeText(getContext(), R.string.battery_usage_app_not_found, Toast.LENGTH_LONG).show();
        }
    }

    private void initHardwareInfoView(View rootView) {

        // temperature
        View tInfoBar = rootView.findViewById(R.id.temperatureInfo);

        ImageView tIcon = (ImageView) tInfoBar.findViewById(R.id.infoIcon);
        tIcon.setImageResource(R.drawable.hardware_info_temperature);

        TextView tLabel = (TextView) tInfoBar.findViewById(R.id.infoLabel);
        tLabel.setText(getString(R.string.temperature_label));

        temperatureValue = (TextView)tInfoBar.findViewById(R.id.infoValue);
        temperatureValue.setText("");

        ImageView tIndicator = (ImageView) tInfoBar.findViewById(R.id.supportIndicator);
        tIndicator.setActivated(true);

        // voltage
        View vInfoBar = rootView.findViewById(R.id.voltageInfo);

        ImageView vIcon = (ImageView) vInfoBar.findViewById(R.id.infoIcon);
        vIcon.setImageResource(R.drawable.hardware_info_voltage);

        TextView vLabel = (TextView) vInfoBar.findViewById(R.id.infoLabel);
        vLabel.setText(getString(R.string.voltage_label));

        voltageValue = (TextView) vInfoBar.findViewById(R.id.infoValue);
        voltageValue.setText("4.2 V");

        ImageView vIndicator = (ImageView) vInfoBar.findViewById(R.id.supportIndicator);
        vIndicator.setActivated(true);

        // health
        View hInfoBar = rootView.findViewById(R.id.healthInfo);

        ImageView hIcon = (ImageView) hInfoBar.findViewById(R.id.infoIcon);
        hIcon.setImageResource(R.drawable.hardware_info_health);

        TextView hLabel = (TextView) hInfoBar.findViewById(R.id.infoLabel);
        hLabel.setText(getString(R.string.health_label));

        healthValue = (TextView) hInfoBar.findViewById(R.id.infoValue);
        healthValue.setText(getString(R.string.health_value_good));

        ImageView hIndicator = (ImageView) hInfoBar.findViewById(R.id.supportIndicator);
        hIndicator.setActivated(true);

        // speedy charge
        View sInfoBar = rootView.findViewById(R.id.speedyChargeInfo);

        ImageView sIcon = (ImageView) sInfoBar.findViewById(R.id.infoIcon);
        sIcon.setImageResource(R.drawable.hardware_info_speedcharge);

        TextView sLabel = (TextView) sInfoBar.findViewById(R.id.infoLabel);
        sLabel.setText(getString(R.string.speedy_charge_label));

        speedyChargeValue = (TextView) sInfoBar.findViewById(R.id.infoValue);
        speedyChargeValue.setText(getString(R.string.support_value_yes));

        ImageView sIndicator = (ImageView) sInfoBar.findViewById(R.id.supportIndicator);
        sIndicator.setActivated(true);
    }

    private void initChargeStatusView(View rootView) {
        ImageView acIcon = (ImageView) rootView.findViewById(R.id.ac_charge_icon);
        ImageView usbIcon = (ImageView) rootView.findViewById(R.id.usb_charge_icon);
        ImageView wirelessIcon = (ImageView) rootView.findViewById(R.id.wireless_charge_icon);
        chargeTypeIcons = new ImageView[] {acIcon, usbIcon, wirelessIcon};
    }

    private void initBatteryView(View rootView) {
        batteryView = (BatteryView) rootView.findViewById(R.id.battery_view);
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

    private void updateChargeType(int newChargeType) {
        if (newChargeType < 0)
            return;

        if (chargeType != newChargeType) {
            if (chargeType != BatteryLogService.BATTERY_NO_CHARGE)
                chargeTypeIcons[chargeType].setActivated(false);

            chargeType = newChargeType;

            if (chargeType != BatteryLogService.BATTERY_NO_CHARGE)
                chargeTypeIcons[chargeType].setActivated(true);
        }
    }

    private void updateFromService() {
//        if (BatteryLogService.getChargeType() == BatteryLogService.BATTERY_AC_CHARGE) {
//            updateStatus("Charging(AC)");
//        }
//        else if (BatteryLogService.getChargeType() == BatteryLogService.BATTERY_USB_CHARGE) {
//            updateStatus("Charging(USB)");
//        }
//        else {
//            updateStatus("No Charge");
//        }
        updateChargeType(BatteryLogService.getChargeType());

        temperatureValue.setText(BatteryLogService.getTemperature() / 10.0f + " " + (char) 0x00B0 + "C");
        voltageValue.setText(String.format("%.2f", BatteryLogService.getVoltage() / 1000.0f) + " V");

        batteryView.setPower(BatteryLogService.getCurrentLevel());

        updateLevel(BatteryLogService.getCurrentLevel() + "%");
//        Log.d(TAG, "updateFromService() level: "+BatteryLogService.getCurrentLevel());
    }
}
