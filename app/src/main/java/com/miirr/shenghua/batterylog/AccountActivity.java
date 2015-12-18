package com.miirr.shenghua.batterylog;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by shenghua on 12/7/15.
 */
public class AccountActivity extends AppCompatActivity {

    public static final String LOGIN_STATE_STORAGE_KEY = "LoggedIn";

    public static final String EXTRA_KEY = "ActivityType";
    public static final int UNDEFINED_ACTIVITY = -1;
    public static final int LOGIN_ACTIVITY = 0;
    public static final int REGISTER_ACTIVITY = 1;

    private int mActivityType;

    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;
    private View mProgressView;
    private View mHostFormView;

    private NetworkTask mNetworkTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mActivityType = intent.getIntExtra(EXTRA_KEY, UNDEFINED_ACTIVITY);

        switch (mActivityType) {
            case LOGIN_ACTIVITY:
                setupLoginActivityView();
                break;

            case REGISTER_ACTIVITY:
                setupRegisterActivityView();
                break;

            default:
                break;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public static boolean isLoggedIn(Context context) {
        PrefsStorageDelegate.initialize(context.getSharedPreferences(
                PrefsStorageDelegate.PREFS_NAME, Context.MODE_PRIVATE));
        return PrefsStorageDelegate.getBooleanValue(LOGIN_STATE_STORAGE_KEY);
    }

    private void attemptLogin() {
        accountOperation(mUsernameView, mPasswordView, null);
    }

    private void attemptRegister() {
        accountOperation(mUsernameView, mPasswordView, mPasswordConfirmView);
    }

    private void accountOperation(EditText usernameView, EditText passwordView, EditText passwordConfirmView) {
        if (mNetworkTask != null) {
            return;
        }

        if (checkInput(usernameView, passwordView, passwordConfirmView)) {
            // Show a progress spinner, and kick off a background login task
            showProgress(true);
            mNetworkTask = new NetworkTask( usernameView.getText().toString(),
                                            passwordView.getText().toString());
            mNetworkTask.execute((Void) null);
        }
    }

    private boolean checkInput(EditText usernameView, EditText passwordView, EditText passwordConfirmView) {

        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);
        if (passwordConfirmView != null) passwordConfirmView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        View checkFailedView = null;

        do {
            // Check for a valid username.
            if (TextUtils.isEmpty(username)) {
                usernameView.setError(getString(R.string.error_field_required));
                checkFailedView = usernameView;
                break;
            } else if (!isValidUsername(username)) {
                usernameView.setError(getString(R.string.error_invalid_username));
                checkFailedView = usernameView;
                break;
            }

            // Check for a valid password
            if (TextUtils.isEmpty(password)) {
                passwordView.setError(getString(R.string.error_field_required));
                checkFailedView = passwordView;
                break;
            } else if (!isValidPassword(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                checkFailedView = passwordView;
                break;
            }

            // check password consistent
            if (passwordConfirmView != null) {
                String passwordConfirm = passwordConfirmView.getText().toString();
                if (!passwordConfirm.equals(password)) {
                    passwordConfirmView.setError(getString(R.string.error_confirm_password));
                    checkFailedView = passwordConfirmView;
                    break;
                }
            }

        } while(false);

        if (checkFailedView != null) {
            checkFailedView.requestFocus();
            return false;
        }
        else {
            return true;
        }
    }

    private boolean isValidUsername(String username) {
        //return username.contains("@");
        return username.length() > 3;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    private void showProgress(final boolean show) {
        AnimationHelper.showNetworkProgress(
                mHostFormView, mProgressView,
                show, getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    private class NetworkTask extends AsyncTask<Void, Void, Integer> {

        private final String mUsername;
        private final String mPassword;

        NetworkTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (mActivityType == LOGIN_ACTIVITY) {
                return WebServerDelegate.getInstance().login(mUsername, mPassword);
            }
            else if (mActivityType == REGISTER_ACTIVITY) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(final Integer resultCode) {
            mNetworkTask = null;
            showProgress(false);

            Log.d("onPostExecute", "resultCode: " + resultCode);

            switch (resultCode) {
                case WebServerDelegate.SERVER_LOGIN_SUCCEEDED:
                case WebServerDelegate.SERVER_LOGIN_ALREADY:
                case WebServerDelegate.SERVER_REGISTER_SUCCEEDED:
                    AccountActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                    finish();
                    break;

                case WebServerDelegate.SERVER_LOGIN_INCORRECT_USER_OR_PASSWORD:
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    break;

                case WebServerDelegate.SERVER_REGISTER_INVALID_USERNAME_OR_PASSWORD:
                    mPasswordView.setError(getString(R.string.error_invalid_username_or_password));
                    mPasswordView.requestFocus();
                    break;

                case WebServerDelegate.SERVER_REGISTER_ACCOUNT_ALREADY_EXISTS:
                    mUsernameView.setError(getString(R.string.error_username_already_exists));
                    mUsernameView.requestFocus();
                    break;

                case WebServerDelegate.SERVER_CSRF_TOKEN_NULL_OR_EMPTY:
                case WebServerDelegate.SERVER_GET_CSRF_TOKEN_FAILED:
                case WebServerDelegate.SERVER_BAD_REQUEST:
                    String message = getResources().getString(R.string.error_server_communication_failed);
                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mNetworkTask = null;
            showProgress(false);
        }
    }
}
