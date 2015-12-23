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
    private Button mButton;

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


//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        String cookieString = getCookies(getContext());
//        Log.d("----->", cookieString);
//        cookieManager.setCookie(URL, cookieString);
//        cookieManager.removeAllCookie();
//        CookieSyncManager.createInstance(getContext()).sync();
//        cookieManager.removeSessionCookie();
//        CookieSyncManager.getInstance().sync();


        mWebView.setWebViewClient(new WebViewClient());
//        mWebView.loadUrl(URL);

        Map<String, String> headers = new HashMap<String, String>();
        String cookieValue = getCookies(getContext());
        //Log.d("----->", cookieValue);
        headers.put("Cookie", cookieValue);
//        mWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url, headers);
//                return false;
//            }
//        });
        mWebView.loadUrl(URL, headers);

        return rootView;
    }

    private String getCookies(Context context) {

        String mergedCookie = "";//"7eeb1890096146dc392b6a2443b0f6475c182de66a05cad95d1062de877488b2a%3A2%3A%7Bi%3A0%3Bs%3A5%3A%22_csrf%22%3Bi%3A1%3Bs%3A32%3A%22rf92s_paqTDULaEwe-S1hChZhZDZE924%22%3B%7D";
        mergedCookie = PrefsStorageDelegate.getStringValue("CsrfCookes");

        String sessionCookie = PrefsStorageDelegate.getStringValue("SessionCookie");
        if (sessionCookie.length() > 0) {
            mergedCookie = mergedCookie + ";" + sessionCookie;
        }
        return mergedCookie;
    }
}
