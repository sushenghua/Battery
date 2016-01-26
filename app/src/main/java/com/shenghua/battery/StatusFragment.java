package com.shenghua.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by shenghua on 11/14/15.
 */
public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";

    // battery data
    private TextView temperatureValue = null;
    private TextView voltageValue = null;
    private TextView healthValue = null;
    private TextView speedyChargeValue = null;

    private IndicatorView temperatureIndicator = null;
    private IndicatorView voltageIndicator = null;
    private IndicatorView healthIndicator = null;
    private IndicatorView speedyChargeIndicator = null;

    // °C or °F
    private static final int CELSIUS_TEMPERATURE = 0;
    private static final int FAHRENHEIT_TEMPERATURE = 1;
    private int temperatureType = CELSIUS_TEMPERATURE;
    private int currentTemperature = BatteryLogService.BATTERY_INVALID_TEMPERATURE;

    // charge status
    private int chargeType = BatteryLogService.BATTERY_NO_CHARGE;
    private ImageView[] chargeTypeIcons = null;
    private BatteryView batteryView = null;

    // broadcast receiver
    private BroadcastReceiver batteryStatusReceiver;

    public static StatusFragment newInstance() {
        StatusFragment fragment = new StatusFragment();
        //Bundle args = new Bundle();
        //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        //fragment.setArguments(args);
        return fragment;
    }

    public StatusFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //registerBatteryStatusReceiver();
        //Log.d(TAG, "------------>onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d(TAG, "------------>onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);

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
//        Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
//        ResolveInfo resolveInfo = getContext().getPackageManager().resolveActivity(powerUsageIntent, 0);
//        if(resolveInfo != null){
//            startActivity(powerUsageIntent);
//        } else {
//            Toast.makeText(getContext(), R.string.battery_usage_app_not_found, Toast.LENGTH_LONG).show();
//        }

        Intent intent = new Intent(getContext(), DebugActivity.class);
        startActivity(intent);
    }

    private void initHardwareInfoView(View rootView) {

        // temperature
        View tInfoBar = rootView.findViewById(R.id.temperatureInfo);

        ImageView tIcon = (ImageView) tInfoBar.findViewById(R.id.infoIcon);
        tIcon.setImageResource(R.drawable.hardware_info_temperature);

        TextView tLabel = (TextView) tInfoBar.findViewById(R.id.infoLabel);
        tLabel.setText(getString(R.string.temperature_label));

        temperatureValue = (TextView)tInfoBar.findViewById(R.id.infoValue);
        temperatureValue.setText("--");

        temperatureIndicator = (IndicatorView) tInfoBar.findViewById(R.id.supportIndicator);

        tInfoBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTemperatureType();
            }
        });

        // voltage
        View vInfoBar = rootView.findViewById(R.id.voltageInfo);

        ImageView vIcon = (ImageView) vInfoBar.findViewById(R.id.infoIcon);
        vIcon.setImageResource(R.drawable.hardware_info_voltage);

        TextView vLabel = (TextView) vInfoBar.findViewById(R.id.infoLabel);
        vLabel.setText(getString(R.string.voltage_label));

        voltageValue = (TextView) vInfoBar.findViewById(R.id.infoValue);
        voltageValue.setText("--");

        voltageIndicator = (IndicatorView) vInfoBar.findViewById(R.id.supportIndicator);

        // health
        View hInfoBar = rootView.findViewById(R.id.healthInfo);

        ImageView hIcon = (ImageView) hInfoBar.findViewById(R.id.infoIcon);
        hIcon.setImageResource(R.drawable.hardware_info_health);

        TextView hLabel = (TextView) hInfoBar.findViewById(R.id.infoLabel);
        hLabel.setText(getString(R.string.health_label));

        healthValue = (TextView) hInfoBar.findViewById(R.id.infoValue);
        healthValue.setText("--");

        healthIndicator = (IndicatorView) hInfoBar.findViewById(R.id.supportIndicator);

        // speedy charge
        View sInfoBar = rootView.findViewById(R.id.speedyChargeInfo);

        ImageView sIcon = (ImageView) sInfoBar.findViewById(R.id.infoIcon);
        sIcon.setImageResource(R.drawable.hardware_info_speedcharge);

        TextView sLabel = (TextView) sInfoBar.findViewById(R.id.infoLabel);
        sLabel.setText(getString(R.string.speedy_charge_label));

        speedyChargeValue = (TextView) sInfoBar.findViewById(R.id.infoValue);
        speedyChargeValue.setText("--");

        speedyChargeIndicator = (IndicatorView) sInfoBar.findViewById(R.id.supportIndicator);

        speedyChargeSupportCheck();
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

    private void speedyChargeSupportCheck() {
        boolean supported = DeviceInfo.supportSpeedyCharge(getResources());

        if (supported) {
            speedyChargeValue.setText(getString(R.string.support_value_yes));
            speedyChargeIndicator.setIndicatorValue(IndicatorView.INDICATOR_SUPPORT);
        } else {
            speedyChargeValue.setText(getString(R.string.support_value_no));
        }
    }

    private void initStaticClasses() {
        PrefsStorageDelegate.initialize(getContext().getSharedPreferences(
                PrefsStorageDelegate.PREFS_NAME, Context.MODE_PRIVATE));
    }

    @Override
    public void onResume() {
        super.onResume();
        initStaticClasses();
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
        if (newChargeType == BatteryLogService.BATTERY_UNDEFINED_CHARGESTATUS)
            return;

        if (chargeType != newChargeType) {
            if (chargeType != BatteryLogService.BATTERY_NO_CHARGE)
                chargeTypeIcons[chargeType].setActivated(false);

            chargeType = newChargeType;

            if (chargeType != BatteryLogService.BATTERY_NO_CHARGE)
                chargeTypeIcons[chargeType].setActivated(true);
        }
    }

    private String temperatureText(int value, int type) {
        if (type == CELSIUS_TEMPERATURE)
            return String.format("%.1f", value / 10.0f) + " " + (char) 0x00B0 + "C";
        else if (type == FAHRENHEIT_TEMPERATURE)
            return String.format("%.1f", (value / 10.0f * 9 / 5 + 32)) + " " + (char) 0x00B0 + "F";
        else
            return "";
    }

    private void switchTemperatureType() {
        if (currentTemperature != BatteryLogService.BATTERY_INVALID_TEMPERATURE) {

            if (temperatureType == CELSIUS_TEMPERATURE)
                temperatureType = FAHRENHEIT_TEMPERATURE;
            else if (temperatureType == FAHRENHEIT_TEMPERATURE)
                temperatureType = CELSIUS_TEMPERATURE;

            temperatureValue.setText(temperatureText(currentTemperature, temperatureType));
        }
    }

    private void updateTemperature(int value) {
        if (value != BatteryLogService.BATTERY_INVALID_TEMPERATURE) {
            temperatureValue.setText(temperatureText(value, temperatureType));
            temperatureIndicator.setIndicatorValue(IndicatorView.INDICATOR_GOOD);
            currentTemperature = value;
        }
    }

    private void updateVoltage(int value) {
        if (value != BatteryLogService.BATTERY_INVALID_VOLTAGE) {
            voltageValue.setText(String.format("%.2f", value / 1000.0f) + " V");
            voltageIndicator.setIndicatorValue(IndicatorView.INDICATOR_GOOD);
        }
    }

    private void updateHealth(int type) {
        switch (type) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthValue.setText(getString(R.string.health_value_good));
                healthIndicator.setIndicatorValue(IndicatorView.INDICATOR_GOOD);
                voltageIndicator.setIndicatorValue(IndicatorView.INDICATOR_GOOD);
                temperatureIndicator.setIndicatorValue(IndicatorView.INDICATOR_GOOD);
                break;

            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthValue.setText(getString(R.string.health_value_overheat));
                healthIndicator.setIndicatorValue(IndicatorView.INDICATOR_ABNORMAL);
                temperatureIndicator.setIndicatorValue(IndicatorView.INDICATOR_WARNING);
                break;

            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthValue.setText(getString(R.string.health_value_dead));
                healthIndicator.setIndicatorValue(IndicatorView.INDICATOR_DEAD);
                break;

            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthValue.setText(getString(R.string.health_value_over_voltage));
                healthIndicator.setIndicatorValue(IndicatorView.INDICATOR_ABNORMAL);
                voltageIndicator.setIndicatorValue(IndicatorView.INDICATOR_WARNING);
                break;

            case BatteryManager.BATTERY_HEALTH_COLD:
                healthValue.setText(getString(R.string.health_value_cold));
                healthIndicator.setIndicatorValue(IndicatorView.INDICATOR_INACTIVE);
                temperatureIndicator.setIndicatorValue(IndicatorView.INDICATOR_INACTIVE);
                break;

            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                break;
        }
    }

    private void updateFromService() {
        //Log.d(TAG, "updateFromService() level: " + BatteryLogService.getCurrentLevel());
        updateChargeType(BatteryLogService.getChargeType());

        updateTemperature(BatteryLogService.getTemperature());

        updateVoltage(BatteryLogService.getVoltage());

        updateHealth(BatteryLogService.getHealth());

        batteryView.setPower(BatteryLogService.getCurrentLevel(), true);
    }
}
