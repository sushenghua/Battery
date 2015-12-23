package com.shenghua.battery;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * Created by shenghua on 11/14/15.
 */
public class LogFragment extends Fragment {

    private WebView mWebView;
    private Button mButton;

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

        class WebInterface {
            Context mContext;

            WebInterface(Context c) {
                mContext = c;
            }

            byte[] getData() {
                return String.valueOf("{type: 'column2d',"+
                         "renderAt: 'chart-container',"+
                         "dataFormat: 'json',"+
                          "dataSource: {"+
                          "chart: {"+
                                    "caption: \"Quarterly sales summary\","+
                                        "numberPrefix: \"$\","+
                                        "decimals: \"2\","+
                                        "forceDecimals: \"1\""+
                            "},"+
                            "data: ["+
                            "{ label: \"Q1\", value: \"213345\"},"+
                            "{ label: \"Q2\", value: \"192672\"},"+
                            "{ label: \"Q3\", value: \"201238\"},"+
                            "{ label: \"Q4\", value: \"209881\"},"+
                            "]"+
                        "};").getBytes();
            }
        }

        mWebView.addJavascriptInterface(new WebInterface(getContext()), "Android");
        mWebView.loadUrl("file:///android_asset/batterylog.html");
//        mWebView.loadUrl("http://192.168.0.150/battery/app/frontend/web");

//        mButton = (Button) rootView.findViewById(R.id.test);
//        mButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("fuck--->", "bad");
//                mWebView.loadUrl("javascript:renderBatteryLog()");
//            }
//        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d("fuck", "bad");
//        mWebView.loadUrl("javascript:renderBatteryLog()");
    }
}
