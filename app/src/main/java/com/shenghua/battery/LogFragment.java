package com.shenghua.battery;

import android.content.Context;
import android.os.Bundle;
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

/**
 * Created by shenghua on 11/14/15.
 */
public class LogFragment extends Fragment {

    private WebView mWebView;

    private static final String URL = "http://192.168.0.150/battery/app/frontend/web";

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
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);

        mWebView.setWebViewClient(new WebViewClient());
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", getCookies(getContext()));
        mWebView.loadUrl(URL, headers);

        return rootView;
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
