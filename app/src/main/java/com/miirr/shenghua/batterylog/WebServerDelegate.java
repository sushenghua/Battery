package com.miirr.shenghua.batterylog;

import android.content.Context;
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
public class WebServerDelegate {

    private static final String TAG = "WebServerDelegate";

    // login
    private static final String LOGIN_URL = "http://192.168.0.150/battery/app/frontend/web/index.php?r=user%2Fsecurity%2Ftest";
    private static final String LOGIN_FORM_USER = "login-form[login]";
    private static final String LOGIN_FORM_PASSWORD = "login-form[password]";
    private static final String LOGIN_FORM_REMEMBER = "login-form[rememberMe]";
    private static final String LOGIN_FORM_AJAX = "ajax";
    private static final String LOGIN_FORM_AJAX_VALUE = "login-form";

    // register
    private static final String REGISTER_URL = "http://192.168.0.150/battery/app/frontend/web/index.php?r=user%2Fsecurity%2Fregister-m";
    private static final String REGISTER_FORM_USER = "register-form[username]";
    private static final String REGISTER_FORM_PASSWORD = "register-form[password]";

    // server response code
    public static final int SERVER_LOGIN_REQUIRED = 200;
    public static final int SERVER_UPLOAD_SUCCEEDED = 201;

    private static WebServerDelegate sInstance = new WebServerDelegate();

    private WebServerDelegate() {
        // debug
        PrefsStorageDelegate.setUsername("demo8888");
        PrefsStorageDelegate.setPassword("demo8888");
    }

    public static WebServerDelegate getInstance() {
        return sInstance;
    }

    public static boolean networkAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public int uploadChargeLog() {
        // pick all local un-uploaded battery charge log, then generate json data, then upload
        return SERVER_LOGIN_REQUIRED;
    }

    public boolean register(String username, String password) {
        boolean registerSucceeded = false;
        if (username.length() > 0 && password.length() > 0) {
            try {
                URL url = new URL(REGISTER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                postRegisterDataToConnection(username, password, connection);

                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "register http response code: " + responseCode);
                if (responseCode == 200) {
                    int serverResponseCode = parseServerResponseCode(connection);
                    Log.d(TAG, "register server response code: " + serverResponseCode);
                    if (serverResponseCode == 100 || serverResponseCode == 101) {
                        String responseCookie = connection.getHeaderField("Set-Cookie");
                        //Log.d(TAG, "session cookie: " + responseCookie);
                        PrefsStorageDelegate.setCookie(responseCookie);
                        PrefsStorageDelegate.setUsername(username);
                        PrefsStorageDelegate.setPassword(password);
                        registerSucceeded = true;
                    } else {
                        Log.d(TAG, "incorrect username or password");
                    }
                } else if (responseCode == 400) {
                    Log.d(TAG, "register failed. Bad request: " + connection.getErrorStream().toString());
                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return registerSucceeded;
    }

    private void postRegisterDataToConnection(String username, String password, HttpURLConnection connection) {
        try {
            DataOutputStream os = new DataOutputStream(connection.getOutputStream());
            os.writeBytes(generateRegisterPostDataString(username, password));
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateRegisterPostDataString(String username, String password) {
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter(REGISTER_FORM_USER, username)
                .appendQueryParameter(REGISTER_FORM_PASSWORD, password);
        return builder.build().getEncodedQuery();
    }

    public boolean login() {
        return login(true, PrefsStorageDelegate.getUsername(), PrefsStorageDelegate.getPassword());
    }

    public boolean login(String username, String password) {
        return login(false, username, password);
    }

    public boolean login(boolean useSession, String username, String password) {
        boolean loginSucceeded = false;
        if ( useSession || (username.length() > 0 && password.length() > 0) ) {
            try {
                URL url = new URL(LOGIN_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");

                connection.setRequestProperty("Connection", "Keep-Alive");
                //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                //connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6");
                //connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                //connection.setFixedLengthStreamingMode(query.getBytes().length);
                if (useSession) {
                    String cachedCookie = PrefsStorageDelegate.getCookie();
                    if (cachedCookie.length() > 0)
                        connection.setRequestProperty("Cookie", cachedCookie);
                }

                connection.setDoOutput(true);
                connection.setDoInput(true);

                postLoginDataToConnection(username, password, connection);

                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "login http response code: " + responseCode);
                if (responseCode == 200) {
                    int serverResponseCode = parseServerResponseCode(connection);
                    Log.d(TAG, "login server response code: " + serverResponseCode);
                    if (serverResponseCode == 100 || serverResponseCode == 101) {
                        String responseCookie = connection.getHeaderField("Set-Cookie");
                        //Log.d(TAG, "session cookie: " + responseCookie);
                        PrefsStorageDelegate.setCookie(responseCookie);
                        PrefsStorageDelegate.setUsername(username);
                        PrefsStorageDelegate.setPassword(password);
                        loginSucceeded = true;
                    } else {
                        Log.d(TAG, "incorrect username or password");
                    }
                } else if (responseCode == 400) {
                    Log.d(TAG, "login failed. Bad request: " + connection.getErrorStream().toString());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return loginSucceeded;
    }

    private void postLoginDataToConnection(String username, String password, HttpURLConnection connection) {
        try {
            DataOutputStream os = new DataOutputStream(connection.getOutputStream());
            os.writeBytes(generateLoginPostDataString(username, password));
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateLoginPostDataString(String username, String password) {
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter(LOGIN_FORM_USER, username)
                .appendQueryParameter(LOGIN_FORM_PASSWORD, password)
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
}
