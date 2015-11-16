package com.miirr.shenghua.batterylog;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by shenghua on 11/16/15.
 */
public class BatteryLogService extends Service {
    private static final String TAG = "BatteryLogService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.hasExtra(BATTERY_SERVICE)) {
            new PostBatteryLogAsync().execute();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class PostBatteryLogAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            postTest();
            return null;
        }

//        @Override
        protected void onPostExecute(String length) {
            BatteryLogService.this.stopSelf();
        }
    }

    private void postTest() {
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
