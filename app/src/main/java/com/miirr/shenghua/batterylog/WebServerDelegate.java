package com.miirr.shenghua.batterylog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by shenghua on 12/4/15.
 */
public class WebServerDelegate {

    private static final String TAG = "WebServerDelegate";

    // csrf and session
    private static final String CSRF_URL = "http://192.168.0.150/battery/app/frontend/web/index.php?r=user%2Fsecurity%2Fcsrf-token-m";
    private static final String CSRF_TOKEN_PARSE_NAME = "token";
    private static final String CSRF_TOKEN_STORE_NAME = "CsrfToken";
    private static final String CSRF_COOKIE_PARSE_NAME = "_csrf";
    private static final String CSRF_FORM_NAME = CSRF_COOKIE_PARSE_NAME;
    private static final String CSRF_COOKIE_STORE_NAME = "CsrfCookes";
    private static final String SESSION_COOKIE_PARSE_NAME = "FRONTENDSESSID";
    private static final String SESSION_COOKIE_STORE_NAME = "SessionCookie";

    // login
    private static final String LOGIN_URL = "http://192.168.0.150/battery/app/frontend/web/index.php?r=user%2Fsecurity%2Flogin-m";
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

                appendCookiesToConnection(connection, true, true);

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
                        storeCookiesFromConnection(connection);
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

    private boolean obtainCsrfToken(boolean useCookies) {
        boolean succeeded = false;
        try {
            URL url = new URL(CSRF_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Connection", "Keep-Alive");
            if (useCookies)
                appendCookiesToConnection(connection, true, true);

            connection.setDoInput(true);

            connection.connect();

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "csrf http response code: " + responseCode);
            if (responseCode == 200) {
                String csrf = parseServerResponseCsrf(connection);
                if (csrf != null) {
                    PrefsStorageDelegate.setStringValue(CSRF_TOKEN_STORE_NAME, csrf);
                    Log.d(TAG, "save csrf token: " + csrf);
                    storeCookiesFromConnection(connection);
                    succeeded = true;
                } else {
                    Log.d(TAG, "get csrf failed");
                }
            } else if (responseCode == 400) {
                Log.d(TAG, "csrf failed. Bad request: " + connection.getErrorStream().toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return succeeded;
    }

    public boolean login(boolean useSession, String username, String password) {
        boolean loginSucceeded = false;

        if (!obtainCsrfToken(true))
            return false;

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

                appendCookiesToConnection(connection, true, useSession);

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
                        storeCookiesFromConnection(connection);
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

    private void storeCookiesFromConnection(HttpURLConnection connection) {

        List<String> cookiesHeader = connection.getHeaderFields().get("Set-Cookie");

        if (cookiesHeader != null) {
            for (String rawCookie : cookiesHeader) {
                String cookie = HttpCookie.parse(rawCookie).get(0).toString();
                if (cookie.startsWith(CSRF_COOKIE_PARSE_NAME)) {
                    PrefsStorageDelegate.setStringValue(CSRF_COOKIE_STORE_NAME, cookie);
                    Log.d(TAG, "save csrf cookie--->"+cookie);
                }
                else if (cookie.startsWith(SESSION_COOKIE_PARSE_NAME)) {
                    PrefsStorageDelegate.setStringValue(SESSION_COOKIE_STORE_NAME, cookie);
                    Log.d(TAG, "save session cookie--->" + cookie);
                }
            }
        }
    }

    private void appendCookiesToConnection(HttpURLConnection connection,
                                           boolean appendCsrfCookie,
                                           boolean appendSessionCookie) {
        String mergedCookie = "";
        if (appendCsrfCookie)
            mergedCookie = PrefsStorageDelegate.getStringValue(CSRF_COOKIE_STORE_NAME);

        if (appendSessionCookie) {
            String sessionCookie = PrefsStorageDelegate.getStringValue(SESSION_COOKIE_STORE_NAME);
            if (sessionCookie.length() > 0) {
                mergedCookie = mergedCookie + ";" + sessionCookie;
            }
        }

        if (mergedCookie.length() > 0) {
            connection.setRequestProperty("Cookie", mergedCookie);
            Log.d(TAG, "--->connection append cookies: "+mergedCookie);
        }
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
                .appendQueryParameter(CSRF_FORM_NAME, PrefsStorageDelegate.getStringValue(CSRF_TOKEN_STORE_NAME))
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

    private String parseJsonResponseCsrf(String responseString) {
        String csrf = null;
        try {

            JSONObject obj = new JSONObject(responseString);
            //Log.d("JSON log: ", obj.getInt("code") + "   <<<<");
            csrf = obj.getString(CSRF_TOKEN_PARSE_NAME);
            //Log.d("---token-------->", csrf);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return csrf;
    }

    private String parseServerResponseCsrf(HttpURLConnection connection) {
        return parseJsonResponseCsrf(getResponseString(connection));
    }
}
