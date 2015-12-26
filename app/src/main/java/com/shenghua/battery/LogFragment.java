package com.shenghua.battery;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.shenghua.battery.chart.BatteryCombinedChart;
import com.shenghua.battery.chart.LogValueFormatter;
import com.shenghua.battery.chart.LogYAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by shenghua on 11/14/15.
 */
public class LogFragment extends Fragment {

    private static final int MAX_VISIBLE_VALUE_COUNT = 50;
    private static final float AXIS_WIDTH = 0.8f;
    private static final float CHART_VALUE_TEXT_SIZE = 10;

    private ListView mListView;
    private BatteryCombinedChart mPowerChart;
    private BatteryCombinedChart mRateChart;

    private ArrayList<String> mXLabels = null;

    private JSONArray mLogs;

    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_log, container, false);

        mListView = (ListView) rootView.findViewById(R.id.chart_list_view);

        int height =  600;
        mPowerChart = createChart(LogYAxisValueFormatter.createPowerAxisValueFormatter());
        mPowerChart.setMinimumHeight(height);
        mPowerChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

            }

            @Override
            public void onNothingSelected() {

            }
        });


        mRateChart = createChart(LogYAxisValueFormatter.createRateAxisValueFormatter(getContext()));
        mRateChart.setMinimumHeight(height);

//        mListView.setAdapter(new CombinedChartAdapter(getContext()));
        mListView.setAdapter(new CombinedChartAdapter());

        fetchLogsFromDb();

        return rootView;
    }

    private class CombinedChartAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 2;
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 0)
                return mPowerChart;
            else if (position == 1)
                return mRateChart;
            return null;
        }
    }

    public BatteryCombinedChart createChart(YAxisValueFormatter leftYAxisFormatter) {

        BatteryCombinedChart chart = new BatteryCombinedChart(getContext());

        chart.setDescription("");
        chart.setMaxVisibleValueCount(MAX_VISIBLE_VALUE_COUNT);

        chart.setPinchZoom(false);
        //chart.setScaleXEnabled(false);
        chart.setDrawValueAboveBar(false);

        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        //mPowerChart.setDrawOrder(new CombinedChart.DrawOrder[]{
        //        CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        //});

        YAxis leftY = chart.getAxisLeft();
        //leftY.setTextColor(Color.LTGRAY);
        leftY.setTextColor(Color.WHITE);
        leftY.setAxisLineWidth(AXIS_WIDTH);
        leftY.setValueFormatter(leftYAxisFormatter);
        //leftY.setDrawGridLines(false);
        //leftY.enableGridDashedLine(10f, 10f, 0);

        YAxis rightY = chart.getAxisRight();
        //rightY.setTextColor(Color.LTGRAY);
        rightY.setTextColor(ContextCompat.getColor(getContext(), R.color.battery_log_charge_duration_color));
        rightY.setAxisLineWidth(AXIS_WIDTH);
        rightY.setValueFormatter(LogYAxisValueFormatter.createDurationAxisValueFormatter(getContext()));
        rightY.setDrawGridLines(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.LTGRAY);
        xAxis.setAxisLineWidth(AXIS_WIDTH);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        //xAxis.enableGridDashedLine(10f, 10f, 10f);

        Legend l = chart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(16f);
        l.setTextColor(Color.LTGRAY);

        // http://developer.android.com/guide/topics/graphics/hardware-accel.html
        // Same type shaders inside ComposeShader not supported by Hardware-acceleration
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        //    mPowerChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //}
        return chart;
    }

    private ArrayList<String> getXLabels(boolean forceUpdate) {

        if (mXLabels == null || forceUpdate) {
            
            mXLabels = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
////            xLabels.add("2015-12-25, 23:00 "+i);
//            xLabels.add(
//                    DateUtils.formatDateTime(getContext(),
//                            System.currentTimeMillis(),
//                            DateUtils.FORMAT_NUMERIC_DATE
//                                    | DateUtils.FORMAT_SHOW_YEAR
//                                    | DateUtils.FORMAT_SHOW_DATE
//                                    | DateUtils.FORMAT_SHOW_TIME));
//        }
            try {
                for (int i = mLogs.length() - 1; i >= 0; --i) {
                    JSONObject log = mLogs.getJSONObject(i);
                    mXLabels.add(
                            DateUtils.formatDateTime(getContext(), log.getLong("bt") * 1000,
                                    DateUtils.FORMAT_NUMERIC_DATE
                                            | DateUtils.FORMAT_SHOW_YEAR
                                            | DateUtils.FORMAT_SHOW_DATE
                                            | DateUtils.FORMAT_SHOW_TIME)
                    );

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mXLabels;
    }

    private BarData getPowerBarData() {

        BarData data = new BarData();

        ArrayList<BarEntry> entries =  new ArrayList<>();
//        for (int i = 0; i < 3; i++) {
//            float val1 = (int) (Math.random() * 100 - 0);
//            float val2 = (int) (Math.random() * (100-Math.abs(val1)));
//            entries.add(new BarEntry(new float[]{val1, val2}, i));
//        }
//        entries.add(new BarEntry(new float[]{3, 4}, 3));
//        entries.add(new BarEntry(new float[]{-4, -1}, 4));
        try {
            for (int i = mLogs.length()-1, j = 0; i >= 0; --i, ++j) {
                JSONObject log = mLogs.getJSONObject(i);
                entries.add(new BarEntry(
                        new float[]{log.getInt("bp"), log.getInt("ep") - log.getInt("bp")}, j)
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BarDataSet entrySet = new BarDataSet(entries, "");

        entrySet.setColors(new int[]{
                ContextCompat.getColor(getContext(), R.color.battery_log_begin_power_color),
                ContextCompat.getColor(getContext(), R.color.battery_log_charged_power_color)});
        entrySet.setValueTextColor(Color.WHITE);
        entrySet.setStackLabels(new String[]{
                getString(R.string.chart_begin_power),
                getString(R.string.chart_charged_power)});

        entrySet.setAxisDependency(YAxis.AxisDependency.LEFT);

        data.addDataSet(entrySet);
        data.setValueTextSize(CHART_VALUE_TEXT_SIZE);
        data.setValueFormatter(LogValueFormatter.createPowerValueFormatter());

        return data;
    }

    private BarData getRateBarData() {

        BarData data = new BarData();
        ArrayList<BarEntry> entries =  new ArrayList<>();
        try {
            for (int i = mLogs.length()-1, j = 0; i >= 0; --i, ++j) {
                JSONObject log = mLogs.getJSONObject(i);
                float duration = log.getLong("et") - log.getLong("bt");
                float rate = duration > 1 ? (log.getInt("ep") - log.getInt("bp")) * 60f / duration : 0;
                rate = Math.round(rate * 100f) / 100f;
                entries.add(new BarEntry(rate, j));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BarDataSet entrySet = new BarDataSet(entries, "");
        entrySet.setColor(ContextCompat.getColor(getContext(), R.color.battery_log_charge_rate_color));
        entrySet.setValueTextColor(Color.WHITE);
        entrySet.setLabel(getString(R.string.chart_charge_rate));

        entrySet.setAxisDependency(YAxis.AxisDependency.LEFT);

        data.addDataSet(entrySet);
        data.setValueTextSize(CHART_VALUE_TEXT_SIZE);
        data.setValueFormatter(LogValueFormatter.createRateValueFormatter(getContext()));

        return data;
    }

    private LineData getDurationLineData() {

        LineData data = new LineData();
        ArrayList<Entry> entries =  new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            float val =(float) (Math.random() * 5 + Math.random()/3);
//            entries.add(new Entry(val, i));
//        }
        try {
            for (int i = mLogs.length()-1, j = 0; i >= 0; --i, ++j) {
                JSONObject log = mLogs.getJSONObject(i);
                entries.add(new Entry((log.getInt("et")-log.getInt("bt"))/60, j));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LineDataSet entrySet = new LineDataSet(entries, getString(R.string.chart_charge_duration));
        int c = ContextCompat.getColor(getContext(), R.color.battery_log_charge_duration_color);
        entrySet.setColor(c);
        entrySet.setCircleColor(c);
        entrySet.setCircleSize(4);
        //set.setCircleColorHole(Color.WHITE);
        //set.setFillColor(Color.TRANSPARENT);
        entrySet.setValueTextColor(Color.WHITE);
        entrySet.setLineWidth(2);
        entrySet.setDrawValues(false);
        entrySet.setHighlightEnabled(false);

        entrySet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        data.addDataSet(entrySet);
        data.setValueTextSize(CHART_VALUE_TEXT_SIZE);
        data.setValueFormatter(LogValueFormatter.createDurationValueFormatter(getContext()));

        return data;
    }

    public void fetchLogsFromDb() {
        BatteryLocalDbAdapter dbAdapter = new BatteryLocalDbAdapter(getContext());
        mLogs = dbAdapter.getLatestChargeLog(7);
        //Log.d("--->fetch db", mLogs.toString());
        if (mLogs == null) {
            mLogs = new JSONArray();
        }
    }

    public void drawChart() {

        CombinedData powerData = new CombinedData(getXLabels(false));
        powerData.setData(getPowerBarData());
        powerData.setData(getDurationLineData());
        mPowerChart.setData(powerData);

        CombinedData rateData = new CombinedData(getXLabels(false));
        rateData.setData(getRateBarData());
        rateData.setData(getDurationLineData());
        mRateChart.setData(rateData);

        mPowerChart.invalidate();
        mRateChart.invalidate();
    }


    @Override
    public void onResume() {
        super.onResume();
        drawChart();
    }
}
