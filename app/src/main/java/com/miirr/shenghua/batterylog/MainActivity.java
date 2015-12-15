package com.miirr.shenghua.batterylog;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private SlidingTabLayout mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(new BatteryView(this));
        setContentView(R.layout.activity_main);

        // ------debug
//        Resources res = getResources();
//        DisplayMetrics dm = res.getDisplayMetrics();
//        Configuration conf = res.getConfiguration();
//        Locale newLocale = new Locale("en");
//        Locale.setDefault(newLocale);
//        conf.locale = newLocale;
//        res.updateConfiguration(conf, dm);
        // ------------

        // fragment adapter
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // setup the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int pos) {
                Log.d("MainActivity", "on page " + pos + " selected");
            }
        });

        // setup sliding tabs
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setCustomTabView(R.layout.tab, R.id.tabText);

        mTabs.setViewPager(mViewPager);

        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(MainActivity.this, R.color.colorAccent);
            }
        });

        // launch battery log service
        Intent serviceIntent = new Intent(this, BatteryLogService.class);
        startService(serviceIntent);


        // database test
        BatteryLocalDbAdapter dbAdapter = new BatteryLocalDbAdapter(this);
//        long id = dbAdapter.insertLog(20, 12032300, 60, 12032330, 12032335, 35, true);
//        Log.d("DB insert test", ""+id);

//        dbAdapter.markChargeLogAsUploaded(dbAdapter.getChargeLog(false));

        JSONArray ja = dbAdapter.getChargeLog(false);
        Log.d("DB query test count", ja.length()+"");
        Log.d("DB query test", ja.toString());
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        int icons[] = {R.drawable.tab_battery, R.drawable.tab_log};
        String[] tabText = getResources().getStringArray(R.array.main_tabs);

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Log.d("pager pos: ", ""+position);
            Fragment a = StatusFragment.newInstance();

            switch (position) {
                case 0:
                    return StatusFragment.newInstance();
                case 1:
                    return LogFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            Drawable drawable = getResources().getDrawable(icons[position]);
            drawable.setBounds(0, 0, 80, 80);

            ImageSpan imageSpan = new ImageSpan(drawable) {
                // change fm will change pos of the text
                @Override
                public int getSize(Paint paint, CharSequence text,
                                   int start, int end,
                                   Paint.FontMetricsInt fm) {

                    Rect rect = getCachedDrawable().getBounds();
                    if (fm != null) {
                        //Log.d("drawable rect", rect.toString());
                        //Log.d("paint font metric", paint.getFontMetricsInt().toString());
                        //Log.d("fm", fm.toString());
                        fm.ascent = fm.top = - rect.bottom/2;
                        fm.descent = fm.bottom = rect.bottom/2;
                        //fm = paint.getFontMetricsInt();
                        //Log.d("change fm", fm.toString());
                        //Log.d("-----", "--------------");
                    }
                    return rect.right;
                }

                @Override
                public void draw(Canvas canvas, CharSequence text, int start,
                                 int end, float x, int top, int y, int bottom,
                                 Paint paint) {
                    Drawable b = getDrawable();
                    canvas.save();
                    //Log.d("ImageSpan", text + "");
                    //Log.d("ImageSpan", "start: " + start + ", end: " + end + ", x: " + x + ", top: " + top + ", y: " + y + ", bottom: " + bottom);
                    //Log.d("ImageSpan", b.getBounds().toString());
                    //Log.d("ImageSpan", paint.getFontMetricsInt().toString());

                    int transY = bottom - b.getBounds().bottom;
                    transY -= paint.getFontMetricsInt().bottom;

                    canvas.translate(x, transY);
                    b.draw(canvas);
                    canvas.restore();
                }

                // Redefined locally because it is a private member from DynamicDrawableSpan
                private Drawable getCachedDrawable() {
                    WeakReference<Drawable> wr = mDrawableRef;
                    Drawable d = null;
                    if (wr != null)
                        d = wr.get();
                    if (d == null) {
                        d = getDrawable();
                        mDrawableRef = new WeakReference<>(d);
                    }
                    return d;
                }
                private WeakReference<Drawable> mDrawableRef;
            };

            SpannableString spannableString = new SpannableString("  " + tabText[position]);
            spannableString.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableString;
        }
    }
}
