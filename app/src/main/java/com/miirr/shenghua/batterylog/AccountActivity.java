package com.miirr.shenghua.batterylog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by shenghua on 12/7/15.
 */
public class AccountActivity extends AppCompatActivity {

    public static final int UNDEFINED_ACTIVITY = -1;
    public static final int LOGIN_ACTIVITY = 0;
    public static final int REGISTER_ACTIVITY = 1;

    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;
    private View mProgressView;
    private View mHostFormView;

    private NetworkTask mNetworkTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        int activityType = intent.getIntExtra("ActivityType", UNDEFINED_ACTIVITY);

        switch (activityType) {
            case LOGIN_ACTIVITY:
                setupLoginActivityView();
                break;

            case REGISTER_ACTIVITY:
                setupRegisterActivityView();
                break;

            default:
                break;
        }
    }

    private void setupLoginActivityView() {

        setContentView(R.layout.activity_login);
        setTitle(R.string.title_activity_login);

        mUsernameView = (EditText) findViewById(R.id.login_username);
        mPasswordView = (EditText) findViewById(R.id.login_password);
        mPasswordConfirmView = null;

        mHostFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // touch this field will remove error on password field
        mUsernameView.setOnTouchListener(new TextView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPasswordView.setError(null);
                return false;
            }
        });

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void setupRegisterActivityView() {

        setContentView(R.layout.activity_register);
        setTitle(R.string.title_activity_register);

        mUsernameView = (EditText) findViewById(R.id.register_username);
        mPasswordView = (EditText) findViewById(R.id.register_password);
        mPasswordConfirmView = (EditText) findViewById(R.id.register_password_confirm);

        mProgressView = findViewById(R.id.register_progress);
        mHostFormView = findViewById(R.id.register_form);

        mPasswordView.setOnTouchListener(new TextView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPasswordConfirmView.setError(null);
                return false;
            }
        });

        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
    }

    private void attemptLogin() {

    }

    private void attemptRegister() {

    }

    private class NetworkTask {
    }
}
