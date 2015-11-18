package com.miirr.shenghua.batterylog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by shenghua on 11/16/15.
 */
public class BatteryLogService extends Service {

    // tag
    private static final String TAG = BatteryLogService.class.getSimpleName();

    // broadcast
    public static final String ACTION_BATTERYSTATUS_CHANGED = "com.miirr.shenghua.batteryLog.CHANGED";
    private static BroadcastReceiver mBatteryReceiver;

    // charge type
    private static final int BATTERY_UNDEFINED_CHARGESTATUS = -1;
    public static final int BATTERY_NO_CHARGE = 0;
    public static final int BATTERY_AC_CHARGE = 1;
    public static final int BATTERY_USB_CHARGE = 2;
    private static int mChargeType = BATTERY_UNDEFINED_CHARGESTATUS;

    // power
    private static final int BATTERY_UNKNOWN_POWER = -1;
    private static int mCurrentPower = BATTERY_UNKNOWN_POWER;
    private static int mPluginPower = BATTERY_UNKNOWN_POWER;
    private static int mPlugoutPower = BATTERY_UNKNOWN_POWER;
    private static long mPluginTime = 0;
    private static long mPlugoutTime = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        if (intent != null && intent.hasExtra(BATTERY_SERVICE)) {

            new SaveBatteryChargeCycleAsync().execute();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
//        Log.d(TAG, "onCreate()");

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
//        Log.d(TAG, "onDestory()");
        unregisterReceiver(mBatteryReceiver);
        mBatteryReceiver = null;
        super.onDestroy();
    }

    public static int getChargeType() {
        return mChargeType;
    }

    public static int getCurrentLevel() {
        return mCurrentPower;
    }

    private void batteryChangeCheck(Intent intent) {
//        Log.d(TAG, "batteryChangeCheck");
        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int chargeType = BATTERY_UNDEFINED_CHARGESTATUS;
            if (isCharging) {
                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) {
                    chargeType = BATTERY_USB_CHARGE;
                } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
                    chargeType = BATTERY_AC_CHARGE;
                }
            } else {
                chargeType = BATTERY_NO_CHARGE;
            } // till here the chargeType is determined instead of BATTERY_UNDEFINED_CHARGESTATUS

            mCurrentPower = calculateBatteryLevel(intent);

            if (chargeType != mChargeType) {
                switch (mChargeType) {
                    case BATTERY_UNDEFINED_CHARGESTATUS: // first time of launch this service
                        if (chargeType == BATTERY_USB_CHARGE || chargeType == BATTERY_AC_CHARGE) {
                            // assume the launch as the plugin
                            mPluginTime = System.currentTimeMillis();
                            mPluginPower = mCurrentPower; //calculateBatteryLevel(intent);
                            Log.d(TAG, "save plugin (launch)");
                        }
                        else { // no power supply
                            // this service launched when having power supply plugged in
                            // as plugin power unknown, meaningless to record the cycle
                        }
                        break;

                    case BATTERY_NO_CHARGE: // chargeType can only be either "usb or ac charge"
                        mPluginTime = System.currentTimeMillis();
                        mPluginPower = mCurrentPower; //calculateBatteryLevel(intent);
                        Log.d(TAG, "save plugin");
                        break;

                    case BATTERY_USB_CHARGE:
                    case BATTERY_AC_CHARGE: // chargeType can only be "no charge", plugin => plugout
                        mPlugoutTime = System.currentTimeMillis();
                        mPlugoutPower = mCurrentPower; //calculateBatteryLevel(intent);
                        if (mPlugoutPower != mPlugoutPower) { // record this charge cycle
                            Log.d(TAG, "save charge cycle data");
                        }
                        // clear charge flag
                        Log.d(TAG, "save plugout");
                        break;
                }
                mChargeType = chargeType;
            }
            broadcastActionBatterystatusChanged();
        }
    }

    private int calculateBatteryLevel(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return ((int) ((level / (float) scale) * 100));
    }

    private void broadcastActionBatterystatusChanged() {
        Intent i = new Intent(ACTION_BATTERYSTATUS_CHANGED);
        sendBroadcast(i);
    }

    private class SaveBatteryChargeCycleAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            saveChargeCycleData();
            return null;
        }

//        @Override
        protected void onPostExecute() {
            BatteryLogService.this.stopSelf();
        }
    }

    private void saveChargeCycleData() {
        Log.d("BatteryPost", "postTest()");
        try {
            URL url = new URL("http://192.168.0.150/battery/app/frontend/web/");
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("POST");

            urlConnection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
            urlConnection.setRequestProperty("ACCEPT_LANGUAGE", "en-US,en;q=0.8,zh-CN;q=0.6");

            urlConnection.setDoOutput(true);
            DataOutputStream oStream = new DataOutputStream(urlConnection.getOutputStream());
            String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
//            oStream.writeBytes(urlParameters);
            oStream.flush();
            oStream.close();

            int responseCode = urlConnection.getResponseCode();

            final StringBuilder output = new StringBuilder("Request URL " + url);
            output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
            output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = "";
            StringBuilder responseOutput = new StringBuilder();
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
            }
            br.close();

            output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

//            MainActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    outputView.setText(output);;
//                }
//            });
        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
