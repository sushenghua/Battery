package com.miirr.shenghua.batterylog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

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
//        if (intent != null && intent.hasExtra(ACTION_BATTERYSTATUS_CHANGED)) {
            new SaveBatteryChargeCycleAsync().execute();
//        }
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


            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                Log.d(TAG, "network available");
            } else {
                Log.d(TAG, "network unavailable");
            }



            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("login-form[login]", "demo8888")
                    .appendQueryParameter("login-form[password]", "demo8888")
                    .appendQueryParameter("login-form[rememberMe]", "0")
                    .appendQueryParameter("ajax", "login-form");
            String query = builder.build().getEncodedQuery();

            
            URL url = new URL("http://192.168.0.150/battery/app/frontend/web/index.php?r=user%2Fsecurity%2Ftest");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
//            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6");
//            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
//            connection.setFixedLengthStreamingMode(query.getBytes().length);
//            connection.setRequestProperty("Cookie", "_csrf=24a580ad1044f30222398aae0d10ed51c8e8574f25491d15214c59637501be41a%3A2%3A%7Bi%3A0%3Bs%3A5%3A%22_csrf%22%3Bi%3A1%3Bs%3A32%3A%22AoaJhzUjpm8L_5wjsEL9HZXpGNvE6cxH%22%3B%7D");
            connection.setRequestProperty("Cookie", "FRONTENDSESSID=10gn6j7nf8surhnvi3fbm81f61");
            connection.setDoOutput(true);
            connection.setDoInput(true);

//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            GZIPOutputStream gzip = new GZIPOutputStream(baos);
////            gzip.write(body.getBytes(Charset.forName("UTF8")));
//            gzip.write(query.getBytes());
//            gzip.close();
//            connection.getOutputStream().write(baos.toByteArray());


            connection.connect();

            DataOutputStream os = new DataOutputStream(connection.getOutputStream());
            os.writeBytes(query);
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();

            Log.d(TAG, "response code: "+responseCode);
            if (responseCode == 400) {
                Log.d(TAG, connection.getErrorStream().toString());
            }


//            final StringBuilder output = new StringBuilder("Request URL " + url);
//            output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
//            output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);


            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String responseCookie = connection.getHeaderField("Set-Cookie");//取到所用的Cookie
            Log.d(TAG,"cookie: " + responseCookie);

            String line = "";
            StringBuilder responseOutput = new StringBuilder();
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
                Log.d(TAG, line);
            }
            br.close();

//            output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());
//            Log.d(TAG, output.toString());
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
