package com.shenghua.battery;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.Locale;

import android.util.DisplayMetrics;

public class LanguageActivity extends AppCompatActivity {

    private int mSelectedLanguageOptionIndex = 0;
    private static int sLanguageOptionIndex = 0;
    private static String[] sLanguageOptions = {"auto", "en", "zh"};
    private static int sAutoLanguageIndex = 1; // default point to "en"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PrefsStorageDelegate.initialize(this.getSharedPreferences(PrefsStorageDelegate.PREFS_NAME, Context.MODE_PRIVATE));
        restorePreferredLanguageIndex();

        setupLanguageOptionsView();

        setTitle(R.string.title_activity_language);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_language, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (sLanguageOptionIndex != mSelectedLanguageOptionIndex) {
                sLanguageOptionIndex = mSelectedLanguageOptionIndex;
                setLocaleWithLanguageIndex(this, sLanguageOptionIndex);
                storePreferredLanguageIndex();
            }
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupLanguageOptionsView() {

        String[] langOptions = {getResources().getString(R.string.lang_auto),
                getResources().getString(R.string.lang_en),
                getResources().getString(R.string.lang_zh)};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, langOptions);
        ListView langListView = (ListView) findViewById(R.id.languageListView);
        langListView.setAdapter(adapter);
        langListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        langListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView clickedView = (CheckedTextView) view;
                clickedView.setChecked(true);
                mSelectedLanguageOptionIndex = position;
            }
        });

        mSelectedLanguageOptionIndex = sLanguageOptionIndex;
        langListView.setItemChecked(mSelectedLanguageOptionIndex, true);
    }

    public static String getPreferedLanguage() {
        if (sLanguageOptionIndex == 0) {
            return sLanguageOptions[sAutoLanguageIndex];
        }
        return sLanguageOptions[sLanguageOptionIndex];
    }

    private static void setLocaleWithLanguageIndex(Context context, int langIndex) {
        Resources res = context.getResources();
        if (langIndex == 0) {
            Resources sysRes = Resources.getSystem();
            Configuration config = sysRes.getConfiguration();
            res.updateConfiguration(config, null);
            String autoLanguage = config.locale.getLanguage();
            //Log.d("-----> auto lang", autoLanguage);
            if (autoLanguage.equals(sLanguageOptions[1]))
                sAutoLanguageIndex = 1;
            else if (autoLanguage.equals(sLanguageOptions[2]))
                sAutoLanguageIndex = 2;
            else
                sAutoLanguageIndex = 1;
        }
        else {
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            Locale newLocale = new Locale(sLanguageOptions[langIndex]);
            Locale.setDefault(newLocale);
            conf.locale = newLocale;
            res.updateConfiguration(conf, dm);
        }
    }

    public static void restorePreferredLanguage(Context context) {
        if (!PrefsStorageDelegate.initialized()) {
            PrefsStorageDelegate.initialize(context.getSharedPreferences(PrefsStorageDelegate.PREFS_NAME, Context.MODE_PRIVATE));
        }
        sLanguageOptionIndex = PrefsStorageDelegate.getLanguageIndex();
        setLocaleWithLanguageIndex(context, sLanguageOptionIndex);
    }

    private static void restorePreferredLanguageIndex() {
        if (PrefsStorageDelegate.initialized()) {
            sLanguageOptionIndex = PrefsStorageDelegate.getLanguageIndex();
        }
    }

    private static void storePreferredLanguageIndex() {
        if (PrefsStorageDelegate.initialized()) {
            PrefsStorageDelegate.setLanguageIndex(sLanguageOptionIndex);
        }
    }
}
