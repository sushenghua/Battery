package com.shenghua.battery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class DebugActivity extends AppCompatActivity {

    private EditText debugText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        debugText = (EditText) findViewById(R.id.debugText);

        BatteryLocalDbAdapter dbAdapter = new BatteryLocalDbAdapter(this);

        debugText.setText(dbAdapter.getLatestChargeLogString(100));

//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < 50; ++i) {
//            sb.append("aa\n");
//        }
//        debugText.setText(sb.toString());
    }
}