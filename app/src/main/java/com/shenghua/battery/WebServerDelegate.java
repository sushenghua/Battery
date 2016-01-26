package com.shenghua.battery;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

    // http response code
    private static final int HTTP_RESPONSE_OK = 200;
    private static final int HTTP_RESPONSE_BAD_REQUEST = 400;
    private static final int HTTP_RESPONSE_FORBIDDEN_REQUEST = 403;

    // server operation response code
    public static final int SERVER_UNKNOWN_ERROR = 5000;
    public static final int SERVER_RESPONSE_PARSE_ERROR = 5500;
    public static final int SERVER_BAD_REQUEST = HTTP_RESPONSE_BAD_REQUEST;
    public static final int SERVER_FORBIDDEN_REQUEST = HTTP_RESPONSE_FORBIDDEN_REQUEST;

    public static final int SERVER_UPLOAD_SUCCEEDED = 3000;
    public static final int SERVER_UPLOAD_LOGIN_REQUIRED = 3001;

    public static final int SERVER_LOGIN_SUCCEEDED = 1000;
    public static final int SERVER_LOGIN_ALREADY = 1001;
    public static final int SERVER_LOGIN_INCORRECT_USER_OR_PASSWORD = 1002;
    public static final int SERVER_LOGIN_EMTPY_USER_OR_PASSWORD = 1003;

    public static final int SERVER_REGISTER_SUCCEEDED = 2000;
    public static final int SERVER_REGISTER_FAILED = 2100;
    public static final int SERVER_REGISTER_EMPTY_EMAIL_OR_PASSWORD = 2001;

    public static final int SERVER_GET_CSRF_TOKEN_SUCCEEDED = 1200;
    public static final int SERVER_GET_CSRF_TOKEN_FAILED = 1201;
    public static final int SERVER_CSRF_TOKEN_NULL_OR_EMPTY = 1202;

    // web root
//    private static final String URL_ROOT = "http://192.168.0.150/battery/app/frontend/web/index.php?";
    private static final String URL_ROOT = "http://120.25.209.190/index.php?";

    // csrf and session
    private static final String CSRF_URL = URL_ROOT + "r=user%2Fsecurity%2Fcsrf-token-m";
    private static final String CSRF_TOKEN_PARSE_NAME = "token";
    private static final String CSRF_TOKEN_STORE_NAME = "CsrfToken";
    private static final String CSRF_COOKIE_PARSE_NAME = "_csrf";
    private static final String CSRF_FORM_NAME = CSRF_COOKIE_PARSE_NAME;
    private static final String CSRF_COOKIE_STORE_NAME = "CsrfCookes";
    private static final String SESSION_COOKIE_PARSE_NAME = "FRONTENDSESSID";
    private static final String SESSION_COOKIE_STORE_NAME = "SessionCookie";

    // login
    private static final String LOGIN_URL = URL_ROOT + "r=user%2Fsecurity%2Flogin-m";
    private static final String LOGIN_FORM_EMAIL = "login-form[login]";
    private static final String LOGIN_FORM_PASSWORD = "login-form[password]";
    private static final String LOGIN_FORM_REMEMBER = "login-form[rememberMe]";
    private static final String LOGIN_FORM_AJAX = "ajax";
    private static final String LOGIN_FORM_AJAX_VALUE = "login-form";

    // register
    private static final String REGISTER_URL = URL_ROOT + "r=user%2Fregistration%2Fregister-m";
    private static final String REGISTER_FORM_EMAIL = "register-form[email]";
    private static final String REGISTER_FORM_PASSWORD = "register-form[password]";
    private static final String REGISTER_FORM_DOB = "register-form[dob]";
    private static final String REGISTER_FORM_GENDER = "register-form[gender]";

    // upload
    private static final String UPLOAD_DATA_TAG = "data";
    private static final String UPLOAD_DEVICE_IDENTITY = "did";
    private static final String LOG_UPLOAD_URL = URL_ROOT + "r=battery%2Fupload-log-m";
    private static final String DEVICE_INFO_UPLOAD_URL = URL_ROOT + "r=battery%2Fupload-data-m";


    private JSONObject errorMessage = null;

    private static WebServerDelegate sInstance = new WebServerDelegate();

    public static WebServerDelegate getInstance() {
        return sInstance;
    }

    public static boolean networkAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public JSONObject getRecentErrorMessage() {
        return errorMessage;
    }

    public int uploadDeviceInfo(JSONArray jsonArray) {
        return uploadJsonArrayData(jsonArray, false, DEVICE_INFO_UPLOAD_URL);
    }

    private static final int SINGLE_UPLOAD_LOG_COUNT = 30;
    public int uploadChargeLog(BatteryLocalDbAdapter db) {
        // pick all local un-uploaded battery charge log, then generate json data, then upload
        int serverResponseCode = SERVER_UNKNOWN_ERROR;
        try {
//            JSONArray logsToUpload;
//            while ((logsToUpload = db.getChargeLog(false, SINGLE_UPLOAD_LOG_COUNT)).length() > 0) {
//                serverResponseCode = uploadJsonArrayData(logsToUpload, true, LOG_UPLOAD_URL);
//                if (serverResponseCode == SERVER_UPLOAD_SUCCEEDED) {
//                    db.markChargeLogAsUploaded(logsToUpload);
//                } else {
//                    break;
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponseCode;
    }

    private int uploadJsonArrayData(JSONArray jsonArray, boolean appendDeviceIdentity, String url) {
        int serverResponseCode = SERVER_UNKNOWN_ERROR;
        do {
            try {
                HttpURLConnection connection = createConnection(url, "POST");
                appendCookiesToConnection(connection, true, true);
                postJsonArrayDataToConnection(jsonArray, appendDeviceIdentity, connection);

                connection.connect();

                int httpResponseCode = connection.getResponseCode();
                //Log.d(TAG, "upload http response code: " + httpResponseCode);
                if (httpResponseCode == HTTP_RESPONSE_OK) {
                    serverResponseCode = parseServerResponseCode(connection);
                    //Log.d(TAG, "upload server response code: " + serverResponseCode);
                    if (serverResponseCode == SERVER_UPLOAD_SUCCEEDED) {
                        //saveCookiesFromConnection(connection);
                    }
                } else if (httpResponseCode == HTTP_RESPONSE_BAD_REQUEST) {
                    serverResponseCode = HTTP_RESPONSE_BAD_REQUEST;
                    //Log.w(TAG, "upload failed. Bad request: " + connection.getErrorStream().toString());
                } else if (httpResponseCode == HTTP_RESPONSE_FORBIDDEN_REQUEST) {
                    serverResponseCode = HTTP_RESPONSE_FORBIDDEN_REQUEST;
                    //Log.d(TAG, "upload failed. Forbidden request: " + connection.getErrorStream().toString());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (false);

        return serverResponseCode;
    }

    private void postJsonArrayDataToConnection(JSONArray jsonArray,
                                               boolean appendDeviceIdentity,
                                               HttpURLConnection connection)
            throws IOException {
        DataOutputStream os = new DataOutputStream(connection.getOutputStream());
        os.writeBytes(generateJsonArrayDataString(jsonArray, appendDeviceIdentity));
        os.flush();
        os.close();
    }

    private String generateJsonArrayDataString(JSONArray jsonArray, boolean appendDeviceIdentity) {
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter(CSRF_FORM_NAME, PrefsStorageDelegate.getStringValue(CSRF_TOKEN_STORE_NAME))
                .appendQueryParameter(UPLOAD_DATA_TAG, jsonArray.toString());
        if (appendDeviceIdentity) {
            builder.appendQueryParameter(UPLOAD_DEVICE_IDENTITY, ""+DeviceInfo.getMacAddressHash());
        }
        return builder.build().getEncodedQuery();
    }

    public int register(String email, String password, long dateOfBirth, int gender) {
        int serverResponseCode = SERVER_UNKNOWN_ERROR;
        do {
            serverResponseCode = obtainCsrfToken(true);
            if (serverResponseCode != SERVER_GET_CSRF_TOKEN_SUCCEEDED) break;

            if (email.length() > 0 && password.length() > 0) {
                try {
                    HttpURLConnection connection = createConnection(REGISTER_URL, "POST");
                    appendCookiesToConnection(connection, true, true);
                    postRegisterDataToConnection(email, password, dateOfBirth, gender, connection);

                    connection.connect();

                    int httpResponseCode = connection.getResponseCode();
                    //Log.d(TAG, "register http response code: " + httpResponseCode);
                    if (httpResponseCode == HTTP_RESPONSE_OK) {
                        serverResponseCode = parseServerResponseCode(connection);
                        //Log.d(TAG, "register server response code: " + serverResponseCode);
                        if (serverResponseCode == SERVER_REGISTER_SUCCEEDED) {
                            saveCookiesFromConnection(connection);
                            saveEmailPassword(email, password);
                            saveAsLoggedIn(true);
                        } else {
                            //Log.d(TAG, "incorrect email or password");
                        }
                    } else if (httpResponseCode == HTTP_RESPONSE_BAD_REQUEST) {
                        serverResponseCode = HTTP_RESPONSE_BAD_REQUEST;
                        //Log.w(TAG, "register failed. Bad request: " + connection.getErrorStream().toString());
                    } else if (httpResponseCode == HTTP_RESPONSE_FORBIDDEN_REQUEST) {
                        serverResponseCode = HTTP_RESPONSE_FORBIDDEN_REQUEST;
                        //Log.d(TAG, "register failed. Forbidden request: " + connection.getErrorStream().toString());
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                serverResponseCode = SERVER_REGISTER_EMPTY_EMAIL_OR_PASSWORD;
            }
        } while (false);
        return serverResponseCode;
    }

    private void postRegisterDataToConnection(String email, String password,
                                              long dateOfBirth, int gender,
                                              HttpURLConnection connection)
            throws IOException {
        DataOutputStream os = new DataOutputStream(connection.getOutputStream());
        os.writeBytes(generateRegisterPostDataString(email, password, dateOfBirth, gender));
        os.flush();
        os.close();
    }

    private String generateRegisterPostDataString(String email, String password, long dateOfBirth, int gender) {
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter(CSRF_FORM_NAME, PrefsStorageDelegate.getStringValue(CSRF_TOKEN_STORE_NAME))
                .appendQueryParameter(REGISTER_FORM_EMAIL, email)
                .appendQueryParameter(REGISTER_FORM_PASSWORD, password)
                .appendQueryParameter(REGISTER_FORM_DOB, ""+dateOfBirth)
                .appendQueryParameter(REGISTER_FORM_GENDER, ""+gender);
        return builder.build().getEncodedQuery();
    }

    public int login() {
        return login(true, PrefsStorageDelegate.getEmail(), PrefsStorageDelegate.getPassword());
    }

    public int login(String email, String password) {
        return login(false, email, password);
    }

    public int login(boolean useSession, String email, String password) {

        int serverResponseCode = SERVER_UNKNOWN_ERROR;
        do {
            serverResponseCode = obtainCsrfToken(true);
            if (serverResponseCode != SERVER_GET_CSRF_TOKEN_SUCCEEDED) break;

            if (useSession || (email.length() > 0 && password.length() > 0)) {
                try {
                    HttpURLConnection connection = createConnection(LOGIN_URL, "POST");
                    appendCookiesToConnection(connection, true, useSession);
                    postLoginDataToConnection(email, password, connection);
                    connection.connect();

                    int httpResponseCode = connection.getResponseCode();
                    //Log.d(TAG, "login http response code: " + httpResponseCode);
                    if (httpResponseCode == HTTP_RESPONSE_OK) {
                        serverResponseCode = parseServerResponseCode(connection);
                        //Log.d(TAG, "login server response code: " + serverResponseCode);
                        if (serverResponseCode == SERVER_LOGIN_SUCCEEDED
                                || serverResponseCode == SERVER_LOGIN_ALREADY) {
                            saveCookiesFromConnection(connection);
                            saveEmailPassword(email, password);
                            saveAsLoggedIn(true);
                        } else if (serverResponseCode == SERVER_LOGIN_INCORRECT_USER_OR_PASSWORD) {
                            //Log.d(TAG, "incorrect email or password");
                        }
                    } else if (httpResponseCode == HTTP_RESPONSE_BAD_REQUEST) {
                        serverResponseCode = HTTP_RESPONSE_BAD_REQUEST;
                        //Log.w(TAG, "login failed. Bad request: " + connection.getErrorStream().toString());
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                serverResponseCode = SERVER_LOGIN_EMTPY_USER_OR_PASSWORD;
            }
        } while (false);

        return serverResponseCode;
    }

    private void postLoginDataToConnection(String email, String password, HttpURLConnection connection)
            throws IOException {
        DataOutputStream os = new DataOutputStream(connection.getOutputStream());
        os.writeBytes(generateLoginPostDataString(email, password));
        os.flush();
        os.close();
    }

    private String generateLoginPostDataString(String email, String password) {
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter(CSRF_FORM_NAME, PrefsStorageDelegate.getStringValue(CSRF_TOKEN_STORE_NAME))
                .appendQueryParameter(LOGIN_FORM_EMAIL, email)
                .appendQueryParameter(LOGIN_FORM_PASSWORD, password)
                .appendQueryParameter(LOGIN_FORM_REMEMBER, "0")
                .appendQueryParameter(LOGIN_FORM_AJAX, LOGIN_FORM_AJAX_VALUE);
        return builder.build().getEncodedQuery();
    }

    private int obtainCsrfToken(boolean useCookies) {
        int code = SERVER_UNKNOWN_ERROR;
        try {
            HttpURLConnection connection = createConnection(CSRF_URL, "GET");
            if (useCookies)
                appendCookiesToConnection(connection, true, true);

            connection.connect();

            int httpResponseCode = connection.getResponseCode();
            //Log.d(TAG, "csrf http response code: " + httpResponseCode);
            if (httpResponseCode == HTTP_RESPONSE_OK) {
                String csrf = parseServerResponseCsrf(connection);
                if (csrf != null && csrf.length() > 0) {
                    saveCsrfToken(csrf);
//debugBase64(csrf);
                    saveCookiesFromConnection(connection);
                    code = SERVER_GET_CSRF_TOKEN_SUCCEEDED;
                } else {
                    code = SERVER_CSRF_TOKEN_NULL_OR_EMPTY;
                    //Log.d(TAG, "get csrf failed, null or empty");
                }
            } else if (httpResponseCode == HTTP_RESPONSE_BAD_REQUEST) {
                code = HTTP_RESPONSE_BAD_REQUEST;
                //Log.d(TAG, "get csrf failed. Bad request: " + connection.getErrorStream().toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code;
    }

    private void debugBase64(String csrf) {
        byte[] rawToken;
        try {
            rawToken = Base64.decode(csrf.replace('.', '+').getBytes("UTF-8"), Base64.DEFAULT);
            //Log.d("rawToken---->", String.valueOf(rawToken));

            String rawTokenStr = String.valueOf(rawToken);
            String mask = rawTokenStr.substring(0, 8);
            String token = rawTokenStr.substring(8);

            int maskLen = mask.length();
            int tokenLen = token.length();

            if (maskLen < tokenLen) {
                mask = mask.concat(token.substring(0, tokenLen - maskLen));
            } else if (maskLen > tokenLen) {
                token = token.concat(mask.substring(0, maskLen - tokenLen));
            }

            byte[] maskBytes = mask.getBytes();
            byte[] tokenBytes = token.getBytes();

            byte[] res = new byte[token.length()];
            for (int i = 0; i < res.length; ++i) {
                res[i] = (byte) ((0xFF & (int) (maskBytes[i])) ^ (0xFF & (int) tokenBytes[i]));
            }

            String aaa = String.valueOf(res);
            //Log.d("token decode----->", aaa);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private HttpURLConnection createConnection(String urlString, String method)
            throws  IOException, MalformedURLException {
        HttpURLConnection connection = null;
        URL url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.setRequestProperty("Connection", "Keep-Alive");
        //connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6");
        connection.setRequestProperty("Accept-Language", LanguageActivity.getPreferedLanguage());
        //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        //connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        //connection.setFixedLengthStreamingMode(query.getBytes().length);
        connection.setRequestMethod(method);
        connection.setDoInput(true);
        if (method.equals("POST"))
            connection.setDoOutput(true);

        return connection;
    }

    public void saveCsrfToken(String token) {
        //Log.d(TAG, "save csrf token: " + token);
        PrefsStorageDelegate.setStringValue(CSRF_TOKEN_STORE_NAME, token);
    }

    public void saveEmailPassword(String email, String password) {
        PrefsStorageDelegate.setEmail(email);
        PrefsStorageDelegate.setPassword(password);
    }

    public void saveAsLoggedIn(boolean loggedIn) {
        PrefsStorageDelegate.setBooleanValue(AccountActivity.LOGIN_STATE_STORAGE_KEY, loggedIn);
    }

    private void saveCookiesFromConnection(HttpURLConnection connection) {

        List<String> cookiesHeader = connection.getHeaderFields().get("Set-Cookie");

        if (cookiesHeader != null) {
            for (String rawCookie : cookiesHeader) {
                String cookie = HttpCookie.parse(rawCookie).get(0).toString();
                if (cookie.startsWith(CSRF_COOKIE_PARSE_NAME)) {
                    PrefsStorageDelegate.setStringValue(CSRF_COOKIE_STORE_NAME, cookie);
                    //Log.d(TAG, "save csrf cookie--->"+cookie);
                }
                else if (cookie.startsWith(SESSION_COOKIE_PARSE_NAME)) {
                    PrefsStorageDelegate.setStringValue(SESSION_COOKIE_STORE_NAME, cookie);
                    //Log.d(TAG, "save session cookie--->" + cookie);
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
            //Log.d(TAG, "--->connection append cookies: "+mergedCookie);
        }
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
        int code = SERVER_RESPONSE_PARSE_ERROR;
        try {
            JSONObject obj = new JSONObject(responseString);
            //Log.d("JSON log: ", obj.getInt("code") + "   <<<<");
            code = obj.getInt("code");
            if (obj.has("errors"))
                errorMessage = obj.getJSONObject("errors");
            else
                errorMessage = null;
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
