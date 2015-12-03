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

import org.json.JSONException;
import org.json.JSONObject;

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

    // server response code
    private static final int SERVER_LOGIN_REQUIRED = 200;
    private static final int SERVER_UPLOAD_SUCCEEDED = 201;

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
                        if (mPlugoutPower != mPluginPower) { // record this charge cycle
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
        boolean uploadSucceeded = false;
        if (networkAvailable()) {
            int code = uploadChargeLog();
            if (code == SERVER_LOGIN_REQUIRED) {
                if (login()) {
                    code = uploadChargeLog();
                    uploadSucceeded = code == SERVER_UPLOAD_SUCCEEDED;
                }
            }
            else {

            }
        } else {
            Log.w(TAG, "network unavailable!");
        }

        if (!uploadSucceeded) {
            // save to local db
            Log.d(TAG, "save charge log to local db");
        }
    }

    private int uploadChargeLog() {

        // pick all local un-uploaded battery charge log, then generate json data, then upload


        return SERVER_LOGIN_REQUIRED;
    }

    private boolean login() {
        boolean loginSucceeded = false;
        try {
            URL url = new URL("http://192.168.0.150/battery/app/frontend/web/index.php?r=user%2Fsecurity%2Ftest");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
//            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
//            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6");
//            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
//            connection.setFixedLengthStreamingMode(query.getBytes().length);
//            connection.setRequestProperty("Cookie", "FRONTENDSESSID=tsemlre24rfqbv7l0vn4du8qm4");

            connection.setDoOutput(true);
            connection.setDoInput(true);

            postLoginDataTo(connection);

            connection.connect();

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "login response code: " + responseCode);
            if (responseCode == 200) {
                int serverResponseCode = parseServerResponseCode(connection);
                if (serverResponseCode == 100 || serverResponseCode == 101) {
                    String responseCookie = connection.getHeaderField("Set-Cookie");
                    Log.d(TAG, "session cookie: " + responseCookie);
                    // save session cookie
                    loginSucceeded = true;
                }
                else {
                    Log.d(TAG, "incorrect username or password");
                }
            }
            else if (responseCode == 400) {
                Log.d(TAG, "login failed. Bad request: " + connection.getErrorStream().toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return loginSucceeded;
    }

    private void postLoginDataTo(HttpURLConnection connection) {
        try {
            DataOutputStream os = new DataOutputStream(connection.getOutputStream());
            os.writeBytes(loginPostDataString());
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loginPostDataString() {
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("login-form[login]", "demo8888")
                .appendQueryParameter("login-form[password]", "demo8888")
                .appendQueryParameter("login-form[rememberMe]", "0")
                .appendQueryParameter("ajax", "login-form");
        return builder.build().getEncodedQuery();
    }

    private String getResponseString(HttpURLConnection connection) {
        String result = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            StringBuilder responseOutput = new StringBuilder();
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
                Log.d(TAG, line);
            }
            br.close();

            result = responseOutput.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private int parseJsonResponseCode(String responseString) {
        int code = -1;
        try {
            JSONObject obj = new JSONObject(responseString);
            Log.d("JSON log: ", obj.getInt("code") + "   <<<<");
            code = obj.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }

    private int parseServerResponseCode(HttpURLConnection connection) {
        return parseJsonResponseCode(getResponseString(connection));
    }

    private boolean networkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected()) {
//            Log.d(TAG, "network available");
//        } else {
//            Log.d(TAG, "network unavailable");
//        }
        return networkInfo != null && networkInfo.isConnected();
    }
}
