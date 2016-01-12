package com.shenghua.battery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.EventListener;

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

    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;

    private int mYear = 1990;
    private int mMonth = 0;
    private int mDay = 1;
    private boolean mHumanFingerFocusPossible = false;
    private EditText mDateOfBirthView;

    private int mGenderValue = -1;
    private TextView mGenderLabel;
    private View mGenderLayout = null;

    private Button mRegisterButton;
    private View mProgressView;
    private View mHostFormView;

    private TextView mRegistrationCompletedText;
    private Button mRegistrationCompletedButton;

    private NetworkTask mNetworkTask = null;

    public interface DatePickedListener extends EventListener {
        void onDatePicked(int year, int month, int day);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private int year = 1990;
        private int month = 0;
        private int day = 1;
        private DatePickedListener datePickedListener = null;

        public void setArgument(Bundle arg) {
            this.year = arg.getInt("year", 1990);
            this.month = arg.getInt("month", 0);
            this.day = arg.getInt("day", 1);
        }

        public void setDatePickedListener(DatePickedListener listener) {
            datePickedListener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            if (datePickedListener != null)
                datePickedListener.onDatePicked(year, month, day);
        }
    }

    public void showDatePickerDialog() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        Bundle arg = new Bundle();
        arg.putInt("year", mYear);
        arg.putInt("month", mMonth);
        arg.putInt("day", mDay);
        datePickerFragment.setArgument(arg);

        datePickerFragment.setDatePickedListener(new DatePickedListener() {
            @Override
            public void onDatePicked(int year, int month, int day) {
                mYear = year; mMonth = month; mDay = day;
                final Calendar c = Calendar.getInstance();
                c.set(year, month, day);
                mDateOfBirthView.setText(DateFormat.getDateInstance().format(c.getTime()));
            }
        });
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.gender_male:
                if (checked) mGenderValue = 0;
                break;

            case R.id.gender_female:
                if (checked) mGenderValue = 1;
                break;

            case R.id.gender_other:
                if (checked) mGenderValue = 2;
                break;
        }

        if (mGenderValue > 0) mGenderLabel.setError(null);
    }

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

        mEmailView = (EditText) findViewById(R.id.login_email);
        mPasswordView = (EditText) findViewById(R.id.login_password);
        mPasswordConfirmView = null;
        mDateOfBirthView = null;
        mGenderLabel = null;

        mHostFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // touch this field will remove error on password field
        mEmailView.setOnTouchListener(new TextView.OnTouchListener() {
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

        mEmailView = (EditText) findViewById(R.id.register_email);
        mPasswordView = (EditText) findViewById(R.id.register_password);
        mPasswordConfirmView = (EditText) findViewById(R.id.register_password_confirm);

        mDateOfBirthView = (EditText) findViewById(R.id.register_dob);
        mDateOfBirthView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mHumanFingerFocusPossible)
                    showDatePickerDialog();
                else
                    mHumanFingerFocusPossible = false;
            }
        });
        mDateOfBirthView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        mDateOfBirthView.setOnTouchListener(new TextView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDateOfBirthView.setError(null);
                mHumanFingerFocusPossible = true;
                return false;
            }
        });
        mGenderLabel = (TextView) findViewById(R.id.register_gender);
        mGenderLayout = findViewById(R.id.register_gender_layout);

        mProgressView = findViewById(R.id.register_progress);
        mHostFormView = findViewById(R.id.register_form);

        mRegistrationCompletedText = (TextView) findViewById(R.id.register_completed_text);
        mRegistrationCompletedButton = (Button) findViewById(R.id.register_completed_button);

        mRegistrationCompletedText.setVisibility(View.GONE);
        mRegistrationCompletedButton.setVisibility(View.GONE);
        mRegistrationCompletedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        mPasswordView.setOnTouchListener(new TextView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPasswordConfirmView.setError(null);
                return false;
            }
        });

        mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
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
        accountOperation(mEmailView, mPasswordView, null, null, null);
    }

    private void attemptRegister() {
        accountOperation(mEmailView, mPasswordView, mPasswordConfirmView, mDateOfBirthView, mGenderLabel);
    }

    private void accountOperation(EditText emailView,
                                  EditText passwordView,
                                  EditText passwordConfirmView,
                                  EditText dobView,
                                  TextView genderLabel) {
        if (mNetworkTask != null) {
            return;
        }

        if (checkInput(emailView, passwordView, passwordConfirmView, dobView, genderLabel)) {
            // Show a progress spinner, and kick off a background login task
            showProgress(true);
            if (dobView != null) {
                final Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                mNetworkTask = new NetworkTask( emailView.getText().toString(),
                                                passwordView.getText().toString(),
                                                c.getTimeInMillis()/1000,
                                                mGenderValue );
            }
            else {
                mNetworkTask = new NetworkTask(emailView.getText().toString(),
                        passwordView.getText().toString(),
                        0, 0);
            }
            mNetworkTask.execute((Void) null);
        }
    }

    private boolean checkInput(EditText emailView,
                               EditText passwordView,
                               EditText passwordConfirmView,
                               EditText dobView,
                               TextView genderLabel) {

        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);
        if (passwordConfirmView != null) passwordConfirmView.setError(null);

        // Store values at the time of the login attempt.
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        View checkFailedView = null;

        do {
            // Check for a valid email.
            if (TextUtils.isEmpty(email)) {
                emailView.setError(getString(R.string.error_field_required));
                checkFailedView = emailView;
                break;
            } else if (!isValidEmail(email)) {
                emailView.setError(getString(R.string.error_invalid_email));
                checkFailedView = emailView;
                break;
            }

            // Check for a valid password
            if (TextUtils.isEmpty(password)) {
                passwordView.setError(getString(R.string.error_field_required));
                checkFailedView = passwordView;
                break;
            } else if (!isPasswordLengthOK(password)) {
                mPasswordView.setError(getString(R.string.error_password_at_least_length));
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

            // check date of birth
            if (dobView != null) {
                String date = dobView.getText().toString();
                if (TextUtils.isEmpty(date)) {
                    dobView.setError(getString(R.string.error_field_required));
                    checkFailedView = dobView;
                    break;
                }
            }

            // check gender
            if (genderLabel != null) {
                if (mGenderValue == -1) { // -1: non-set default value
                    genderLabel.setError(getString(R.string.error_field_required));
                    checkFailedView = genderLabel;
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

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordLengthOK(String password) {
        return password.length() >= 6;
    }

    private void showProgress(final boolean show) {
        AnimationHelper.showNetworkProgress(
                mHostFormView, mProgressView,
                show, getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    private void presentConfirmationDialog() {
        mEmailView.setVisibility(View.GONE);
        mPasswordView.setVisibility(View.GONE);
        mPasswordConfirmView.setVisibility(View.GONE);
        mRegisterButton.setVisibility(View.GONE);
        mDateOfBirthView.setVisibility(View.GONE);
        mGenderLayout.setVisibility(View.GONE);

        mRegistrationCompletedText.setVisibility(View.VISIBLE);
        mRegistrationCompletedButton.setVisibility(View.VISIBLE);
    }

    private class NetworkTask extends AsyncTask<Void, Void, Integer> {

        private final String mEmail;
        private final String mPassword;
        private final long mDateOfBirth;
        private final int mGender;

        NetworkTask(String email, String password, long dateOfBirth, int gender) {
            mEmail = email;
            mPassword = password;
            mDateOfBirth = dateOfBirth;
            mGender = gender;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (mActivityType == LOGIN_ACTIVITY) {
                return WebServerDelegate.getInstance().login(mEmail, mPassword);
            }
            else if (mActivityType == REGISTER_ACTIVITY) {
                return WebServerDelegate.getInstance().register(mEmail, mPassword, mDateOfBirth, mGender);
            }

            return UNDEFINED_ACTIVITY;
        }

        @Override
        protected void onPostExecute(final Integer resultCode) {
            mNetworkTask = null;
            showProgress(false);
            //Log.d("onPostExecute", "resultCode: " + resultCode);
            switch (resultCode) {
                case WebServerDelegate.SERVER_LOGIN_SUCCEEDED:
                case WebServerDelegate.SERVER_LOGIN_ALREADY:
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

                case WebServerDelegate.SERVER_REGISTER_SUCCEEDED:
                    AccountActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            presentConfirmationDialog();
                        }
                    });
                    break;

                case WebServerDelegate.SERVER_LOGIN_INCORRECT_USER_OR_PASSWORD:
                    mPasswordView.setError(getString(R.string.error_email_password_mismatch));
                    mPasswordView.requestFocus();
                    break;

                case WebServerDelegate.SERVER_REGISTER_FAILED:
                    JSONObject errors = WebServerDelegate.getInstance().getRecentErrorMessage();
                    try {
                        if (errors.has("email")) {
                            mEmailView.setError(errors.getString("email"));
                            mEmailView.requestFocus();
                        }
                        if (errors.has("password")) {
                            mPasswordView.setError(errors.getString("password"));
                            mPasswordView.requestFocus();
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case WebServerDelegate.SERVER_LOGIN_EMTPY_USER_OR_PASSWORD:
                case WebServerDelegate.SERVER_REGISTER_EMPTY_EMAIL_OR_PASSWORD:
                    String message = getResources().getString(R.string.error_empty_email_or_password);
                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                    break;

                case WebServerDelegate.SERVER_CSRF_TOKEN_NULL_OR_EMPTY:
                case WebServerDelegate.SERVER_GET_CSRF_TOKEN_FAILED:
                case WebServerDelegate.SERVER_BAD_REQUEST:
                    message = getResources().getString(R.string.error_server_communication_failed);
                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                    break;

                case WebServerDelegate.SERVER_FORBIDDEN_REQUEST:
                    message = getResources().getString(R.string.error_server_forbidden);
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
