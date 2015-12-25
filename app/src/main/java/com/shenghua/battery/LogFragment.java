package com.shenghua.battery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by shenghua on 11/14/15.
 */
public class LogFragment extends Fragment {

    private WebView mWebView;

    private static final String URL = "http://192.168.0.150/battery/app/frontend/web/index.php?r=battery%2Fchart";

    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_log, container, false);

        mWebView = (WebView) rootView.findViewById(R.id.webView);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
//        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);

        mWebView.setWebViewClient(new WebViewClient());

        return rootView;
    }

    private void loadPage() {
        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookie();
        cm.setAcceptCookie(true);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", getCookies(getContext()));
        mWebView.loadUrl(URL, headers);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPage();
//        new Timer().schedule(
//                new TimerTask() {
//                    @Override
//                    public void run() {
//                        LogFragment.this.getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                loadPage();
//                            }
//                        });
//                    }
//                },
//                2000
//        );
    }

    private String getCookies(Context context) {

        String mergedCookie = PrefsStorageDelegate.getStringValue("CsrfCookes");

        String sessionCookie = PrefsStorageDelegate.getStringValue("SessionCookie");
        if (sessionCookie.length() > 0) {
            mergedCookie = mergedCookie + ";" + sessionCookie;
        }
        return mergedCookie;
    }
}
