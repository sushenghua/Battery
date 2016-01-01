package com.shenghua.battery;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shenghua on 11/16/15.
 */
public class BatteryLogService extends Service {

    // tag
    private static final String TAG = BatteryLogService.class.getSimpleName();

    // broadcast
    public static final String ACTION_BATTERYSTATUS_CHANGED = "com.shenghua.battery.CHANGED";
    private static BroadcastReceiver mBatteryReceiver;

    // charge type
    public static final int BATTERY_UNDEFINED_CHARGESTATUS = -1;
    public static final int BATTERY_NO_CHARGE = 10;
    public static final int BATTERY_AC_CHARGE = 0;
    public static final int BATTERY_USB_CHARGE = 1;
    public static final int BATTERY_WIRELESS_CHARGE = 2;
    //private static int currentChargeType = BATTERY_UNDEFINED_CHARGESTATUS;

    // temperature, voltage, health consts
    public static final int BATTERY_INVALID_TEMPERATURE = -100;
    public static final int BATTERY_INVALID_VOLTAGE = -1;

    // temperature, voltage, health value
    private static int temperature;
    private static int voltage;
    private static int health;

    // power
    public static final int BATTERY_POWER_UNKNOWN = -1;
    private static int mCurrentPower = BATTERY_POWER_UNKNOWN;
    private static int mPluginPower = BATTERY_POWER_UNKNOWN;
    private static int mPlugoutPower = BATTERY_POWER_UNKNOWN;

    public static final int BATTERY_TIME_UNDEFINED = 0;
    private static long mPluginTime = BATTERY_TIME_UNDEFINED;
    private static long mPlugoutTime = BATTERY_TIME_UNDEFINED;
    private static long mChargeFullTime = BATTERY_TIME_UNDEFINED;

    // web server
    private WebServerDelegate mWebServer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        uploadDeviceInfo();

//        if (intent != null && intent.hasExtra(ACTION_BATTERYSTATUS_CHANGED)) {
//            new SaveBatteryChargeCycleAsync().execute();
//        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //Log.d(TAG, "onCreate()");

        PrefsStorageDelegate.initialize(getApplicationContext().getSharedPreferences(
                PrefsStorageDelegate.PREFS_NAME, Context.MODE_PRIVATE));
        mWebServer = WebServerDelegate.getInstance();

        if (null == mBatteryReceiver) {
            mBatteryReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    batteryChangeCheck(intent);
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent intent = registerReceiver(mBatteryReceiver, filter);
            batteryChangeCheck(intent);
        }
    }

    public void onDestory() {
        //Log.d(TAG, "onDestory()");
        unregisterReceiver(mBatteryReceiver);
        mBatteryReceiver = null;
        super.onDestroy();
    }

    public static int getChargeType() {
        return PrefsStorageDelegate.getChargeType();
    }

    public static int getCurrentLevel() {
        return mCurrentPower;
    }

    public static int getTemperature() {
        return temperature;
    }

    public static int getVoltage() {
        return voltage;
    }

    public static int getHealth() {
        return health;
    }

    private void batteryChangeCheck(Intent intent) {
        //Log.d(TAG, "batteryChangeCheck");
        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {

            temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, BATTERY_INVALID_TEMPERATURE);
            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, BATTERY_INVALID_VOLTAGE);
            health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH,
                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE);

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isFull = status == BatteryManager.BATTERY_STATUS_FULL;
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || isFull;

            int newChargeType = BATTERY_UNDEFINED_CHARGESTATUS;
            if (isCharging) {
                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) {
                    newChargeType = BATTERY_USB_CHARGE;
                } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
                    newChargeType = BATTERY_AC_CHARGE;
                } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
                    newChargeType = BATTERY_WIRELESS_CHARGE;
                }
            } else {
                newChargeType = BATTERY_NO_CHARGE;
            } // till here the chargeType is determined instead of BATTERY_UNDEFINED_CHARGESTATUS

            mCurrentPower = calculateBatteryLevel(intent);
            long timeNow = (long)(System.currentTimeMillis() / 1000.0);

            checkChargeFull(mCurrentPower, timeNow, false);

            int currentChargeType = PrefsStorageDelegate.getChargeType();
            if (newChargeType != currentChargeType) {
                switch (currentChargeType) {
                    case BATTERY_UNDEFINED_CHARGESTATUS: // first time of launch this service
                        if (newChargeType == BATTERY_USB_CHARGE
                                || newChargeType == BATTERY_AC_CHARGE
                                || newChargeType == BATTERY_WIRELESS_CHARGE) {
                            // assume the launch as the plugin
                            Log.d(TAG, "--->launch (as plugin)");
                            PrefsStorageDelegate.setPluginPower(mCurrentPower);
                            PrefsStorageDelegate.setPluginTime(timeNow);
                            checkChargeFull(mCurrentPower, timeNow, true);
                        }
                        else { // no power supply
                            // this service launched when having power supply plugged in
                            // as plugin power unknown, meaningless to record the cycle
                        }
                        break;

                    case BATTERY_NO_CHARGE: // chargeType can only be either "usb or ac charge"
                        Log.d(TAG, "--->plugin");
                        PrefsStorageDelegate.setPluginPower(mCurrentPower);
                        PrefsStorageDelegate.setPluginTime(timeNow);
                        checkChargeFull(mCurrentPower, timeNow, true);
                        break;

                    case BATTERY_USB_CHARGE:
                    case BATTERY_AC_CHARGE: // chargeType can only be "no charge", plugin => plugout
                    case BATTERY_WIRELESS_CHARGE:
                        Log.d(TAG, "--->plugout");
                        mPlugoutPower = mCurrentPower;
                        mPlugoutTime = timeNow;
                        mPluginPower = PrefsStorageDelegate.getPluginPower();
                        mPluginTime = PrefsStorageDelegate.getPluginTime();

                        if (mPlugoutPower > mPluginPower) // record this charge cycle
                            new SaveBatteryChargeCycleAsync().execute();

                        // clear charge flag
                        PrefsStorageDelegate.setPluginTime(BATTERY_TIME_UNDEFINED);
                        PrefsStorageDelegate.setPluginPower(BATTERY_POWER_UNKNOWN);
                        PrefsStorageDelegate.setChargeFullTime(BATTERY_TIME_UNDEFINED);

                        break;
                }
                //currentChargeType = newChargeType;
                PrefsStorageDelegate.setChargeType(newChargeType);
            }
            broadcastActionBatterystatusChanged();
        }
    }

    private void checkChargeFull(int power, long time, boolean overwriteOnExisted) {
        if (power == 100 ) {
            boolean nonexisted = PrefsStorageDelegate.getChargeFullTime() == BATTERY_TIME_UNDEFINED;
            if (overwriteOnExisted || nonexisted)
                PrefsStorageDelegate.setChargeFullTime(time);
        } else { // not full anymore
            PrefsStorageDelegate.setChargeFullTime(BATTERY_TIME_UNDEFINED);
        }
    }

    private int calculateBatteryLevel(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return  (int) (Math.round((float)level / scale * 100));
    }

    private void broadcastActionBatterystatusChanged() {
        Intent i = new Intent(ACTION_BATTERYSTATUS_CHANGED);
        sendBroadcast(i);
    }

    private class SaveBatteryChargeCycleAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            saveChargeCycleDataSync();
            return null;
        }

        //protected void onPostExecute() { // only effective when task launched in main UI thread
        //    BatteryLogService.this.stopSelf();
        //}
    }

    private void saveChargeCycleDataSync() {

        // save to local db anyway
        Log.d(TAG, "--->save charge log to local db");
        BatteryLocalDbAdapter dbAdapter = new BatteryLocalDbAdapter(this);
        long chargeFullTime = PrefsStorageDelegate.getChargeFullTime();
        long chargeEndTime = (mPlugoutPower == 100 && chargeFullTime != BATTERY_TIME_UNDEFINED)?
                chargeFullTime : mPlugoutTime;
        if (chargeEndTime < mPluginTime) // plugin time or full time is problematic
            chargeEndTime = mPlugoutTime;
        dbAdapter.insertLog(mPluginPower, mPluginTime,
                            mPlugoutPower, chargeEndTime,
                            mPlugoutTime, chargeEndTime - mPluginTime, false);

        // try to upload log to server
        boolean uploadSucceeded = false;
        if (WebServerDelegate.networkAvailable(this)) {
            int code = mWebServer.uploadChargeLog(dbAdapter);
            if (code == WebServerDelegate.SERVER_UPLOAD_LOGIN_REQUIRED
                    || code == WebServerDelegate.SERVER_FORBIDDEN_REQUEST) {
                Log.d(TAG, "login required / forbidden request");
                code = mWebServer.login();
                if (code == WebServerDelegate.SERVER_LOGIN_SUCCEEDED
                        || code == WebServerDelegate.SERVER_LOGIN_ALREADY) {
                    Log.d(TAG, "login successful with code: "+code);
                    code = mWebServer.uploadChargeLog(dbAdapter);
                    uploadSucceeded = code == WebServerDelegate.SERVER_UPLOAD_SUCCEEDED;
                    Log.d(TAG, "upload " + (uploadSucceeded ? "succeeded" : "failed"));
                }
                else {
                    Log.d(TAG, "login failed");
                }
            }
            else {
                Log.d(TAG, "upload log error with code: "+code);
            }
        } else {
            Log.w(TAG, "network unavailable!");
        }
    }

    // --- device info
    private void uploadDeviceInfo() {
        if (PrefsStorageDelegate.getDeviceInfoNeedUploadFilter() != DeviceInfo.DEVICE_INFO_NOTHING) {
            new UploadDeviceInfoAsync().execute();
        }
    }

    private class UploadDeviceInfoAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            uploadDeviceInfoSync();
            return null;
        }
    }

    private void uploadDeviceInfoSync() {

        int filter = PrefsStorageDelegate.getDeviceInfoNeedUploadFilter();
        if (filter == DeviceInfo.DEVICE_INFO_NOTHING)
            return;

        boolean uploadSucceeded = false;
        if (WebServerDelegate.networkAvailable(this)) {

            JSONObject deviceInfo = DeviceInfo.getDeviceInfo(BatteryLogService.this, filter);
            int actualUploadInfo = DeviceInfo.DEVICE_INFO_NOTHING;
            try {
                actualUploadInfo = deviceInfo.getInt(DeviceInfo.DEVICE_INFO_FILTER_JSON_KEY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (actualUploadInfo == DeviceInfo.DEVICE_INFO_NOTHING)
                return;

            JSONArray wrap = new JSONArray();
            wrap.put(deviceInfo);

            int code = mWebServer.uploadDeviceInfo(wrap);
            if (code == WebServerDelegate.SERVER_UPLOAD_LOGIN_REQUIRED
                    || code == WebServerDelegate.SERVER_FORBIDDEN_REQUEST) {
                Log.d(TAG, "login required / forbidden request");
                code = mWebServer.login();
                if (code == WebServerDelegate.SERVER_LOGIN_SUCCEEDED
                        || code == WebServerDelegate.SERVER_LOGIN_ALREADY) {
                    Log.d(TAG, "login successful with code: "+code);
                    code = mWebServer.uploadDeviceInfo(wrap);
                }
                else {
                    Log.d(TAG, "login failed");
                }
            }
            uploadSucceeded = code == WebServerDelegate.SERVER_UPLOAD_SUCCEEDED;
            if (uploadSucceeded) {
                Log.d(TAG, "upload device info " + (uploadSucceeded ? "succeeded" : "failed"));
                //int stillNeedUploadInfo = (actualUploadInfo ^ DeviceInfo.DEVICE_FULL_INFO) & filter;
                int stillNeedUploadInfo = actualUploadInfo ^ filter;
                PrefsStorageDelegate.setDeviceInfoNeedUploadFilter(stillNeedUploadInfo);
            }
            else {
                Log.d(TAG, "upload device info failed with code: "+code);
            }
        } else {
            Log.w(TAG, "network unavailable!");
        }
    }
}
