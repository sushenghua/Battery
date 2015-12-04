package com.miirr.shenghua.batterylog;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by shenghua on 12/4/15.
 */
public class WebServerDelegate{

    private static final String TAG = "WebServerDelegate";
    private static final String LOGIN_URL = "http://192.168.0.150/battery/app/frontend/web/index.php?r=user%2Fsecurity%2Ftest";

    private static final String LOGIN_FORM_USER = "login-form[login]";
    private static final String LOGIN_FORM_PASSWORD = "login-form[password]";
    private static final String LOGIN_FORM_REMEMBER = "login-form[rememberMe]";
    private static final String LOGIN_FORM_AJAX = "ajax";
    private static final String LOGIN_FORM_AJAX_VALUE = "login-form";

    // server response code
    public static final int SERVER_LOGIN_REQUIRED = 200;
    public static final int SERVER_UPLOAD_SUCCEEDED = 201;

    public WebServerDelegate(Context context) {
        PreferencesStorageDelegate.setPreferences(context.getSharedPreferences(PreferencesStorageDelegate.PREFS_NAME, Context.MODE_PRIVATE));
        PreferencesStorageDelegate.setUsername("demo8888");
        PreferencesStorageDelegate.setPassword("demo8888");
    }

    public int uploadChargeLog() {
        // pick all local un-uploaded battery charge log, then generate json data, then upload
        return SERVER_LOGIN_REQUIRED;
    }

    public boolean login() {
        boolean loginSucceeded = false;
        try {
            URL url = new URL(LOGIN_URL);
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
            String cachedCookie = PreferencesStorageDelegate.getCookie();
            if (cachedCookie.length() > 0) connection.setRequestProperty("Cookie", cachedCookie);

            connection.setDoOutput(true);
            connection.setDoInput(true);

            postLoginDataTo(connection);

            connection.connect();

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "login http response code: " + responseCode);
            if (responseCode == 200) {
                int serverResponseCode = parseServerResponseCode(connection);
                Log.d(TAG, "login server response code: " + serverResponseCode);
                if (serverResponseCode == 100 || serverResponseCode == 101) {
                    String responseCookie = connection.getHeaderField("Set-Cookie");
                    //Log.d(TAG, "session cookie: " + responseCookie);
                    PreferencesStorageDelegate.setCookie(responseCookie);
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
            os.writeBytes(getLoginPostDataString());
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLoginPostDataString() {
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter(LOGIN_FORM_USER, PreferencesStorageDelegate.getUsername())
                .appendQueryParameter(LOGIN_FORM_PASSWORD, PreferencesStorageDelegate.getPassword())
                .appendQueryParameter(LOGIN_FORM_REMEMBER, "0")
                .appendQueryParameter(LOGIN_FORM_AJAX, LOGIN_FORM_AJAX_VALUE);
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
                //Log.d(TAG, line);
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
            //Log.d("JSON log: ", obj.getInt("code") + "   <<<<");
            code = obj.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }

    private int parseServerResponseCode(HttpURLConnection connection) {
        return parseJsonResponseCode(getResponseString(connection));
    }

//    private boolean networkAvailable() {
//        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
////        if (networkInfo != null && networkInfo.isConnected()) {
////            Log.d(TAG, "network available");
////        } else {
////            Log.d(TAG, "network unavailable");
////        }
//        return networkInfo != null && networkInfo.isConnected();
//    }

    static class PreferencesStorageDelegate {

        private static final String PREFS_NAME = "BatteryLogData";
        private static SharedPreferences prefs;

        private static final String USER_NAME = "UserName";
        private static final String PASSWORD = "Password";
        private static final String COOKIE = "Cookie";

        public static void setPreferences(SharedPreferences p) {
            prefs = p;
        }

        public static String getUsername() {
            return prefs.getString(USER_NAME, "");
        }

        public static String getPassword() {
            return prefs.getString(PASSWORD, "");
        }

        public static String getCookie() {
            return prefs.getString(COOKIE, "");
        }

        public static void setUsername(String username) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(USER_NAME, username);
            editor.commit();
        }

        public static void setPassword(String password) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PASSWORD, password);
            editor.commit();
        }

        public static void setCookie(String cookie) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(COOKIE, cookie);
            editor.commit();
        }
    }
}
