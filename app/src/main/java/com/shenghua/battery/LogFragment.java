package com.shenghua.battery;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
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
import com.shenghua.battery.chart.BatteryCombinedChart;
import com.shenghua.battery.chart.LogValueFormatter;
import com.shenghua.battery.chart.LogYAxisValueFormatter;

import java.util.ArrayList;

/**
 * Created by shenghua on 11/14/15.
 */
public class LogFragment extends Fragment {

    private CombinedChart mChart;

    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_log, container, false);

        mChart = (BatteryCombinedChart) rootView.findViewById(R.id.chart);

        mChart.setDescription("");
        mChart.setMaxVisibleValueCount(60);

        mChart.setPinchZoom(false);
//        mChart.setScaleXEnabled(false);
        mChart.setDrawValueAboveBar(false);

        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);

//        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
//                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
//        });

        float axisLineWidth = 0.8f;

        mChart.getAxisLeft().setValueFormatter(LogYAxisValueFormatter.createPowerAxisValueFormatter());
        mChart.getAxisLeft().setTextColor(Color.LTGRAY);
        mChart.getAxisLeft().setAxisLineWidth(axisLineWidth);
//        mChart.getAxisLeft().setDrawGridLines(false);
//        mChart.getAxisLeft().enableGridDashedLine(10f, 10f, 0);

        mChart.getAxisRight().setValueFormatter(LogYAxisValueFormatter.createDurationAxisValueFormatter());
        mChart.getAxisRight().setTextColor(Color.LTGRAY);
        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getAxisRight().setAxisLineWidth(axisLineWidth);

        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setTextColor(Color.LTGRAY);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setAxisLineWidth(axisLineWidth);
//        mChart.getXAxis().enableGridDashedLine(10f, 10f, 10f);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(16f);
        l.setTextColor(Color.LTGRAY);

        // http://developer.android.com/guide/topics/graphics/hardware-accel.html
        // Same type shaders inside ComposeShader not supported by Hardware-acceleration
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            mChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }

        return rootView;
    }

    public BarData generateTestData() {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            xVals.add("col "+i);
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < 5; i++) {
            float mult = 5;
            float val1 = (int) (Math.random() * mult) + mult / 1;
//            val1 = 100f;
            float val2 = (int) (Math.random() * mult) + mult / 1;

            yVals1.add(new BarEntry(new float[] { val1, val2 }, i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "");
        set1.setColors(getColors());
        set1.setValueTextColor(Color.WHITE);
        set1.setStackLabels(new String[]{getString(R.string.begin_power), getString(R.string.charged_power)});

        ArrayList<BarEntry> yVals2 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            float val = (float) (Math.random() * 5) + 3;
            yVals2.add(new BarEntry(val, i));
        }
        BarDataSet set2 = new BarDataSet(yVals2, "Company B");
        set2.setColor(Color.rgb(164, 228, 251));

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        dataSets.add(set2);
        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10);
        data.setValueFormatter(LogValueFormatter.createPowerValueFormatter());

        return data;
    }

    public void drawChart() {

        ArrayList<String> xLabels = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            xLabels.add("col "+i);
        }


        CombinedData data = new CombinedData(xLabels);
        data.setData(generatePowerBarData());
        data.setData(generateDurationLineData());

//        mChart.setDrawValueAboveBar(true);

        mChart.setData(data);
        mChart.invalidate();
    }

    private BarData generatePowerBarData() {

        BarData data = new BarData();

        ArrayList<BarEntry> entries =  new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            float val1 = (int) (Math.random() * 100 - 0);
            float val2 = (int) (Math.random() * (100-Math.abs(val1)));
            entries.add(new BarEntry(new float[]{val1, val2}, i));
        }

        BarDataSet set = new BarDataSet(entries, "");
        set.setColors(getColors());
        set.setValueTextColor(Color.WHITE);
        set.setStackLabels(new String[]{getString(R.string.begin_power), getString(R.string.charged_power)});

        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        data.addDataSet(set);
        data.setValueTextSize(10);
        data.setValueFormatter(LogValueFormatter.createPowerValueFormatter());

        return data;
    }

    private LineData generateDurationLineData() {
        LineData data = new LineData();

        ArrayList<Entry> entries =  new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            float val =(float) (Math.random() * 5 + Math.random()/3);
            entries.add(new Entry(val, i));
        }

        LineDataSet set = new LineDataSet(entries, "Duration");
        set.setColor(Color.rgb(164, 228, 251));
        set.setFillColor(Color.TRANSPARENT);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);

        set.setAxisDependency(YAxis.AxisDependency.RIGHT);

        data.addDataSet(set);
        data.setValueTextSize(10);

        return data;
    }

    private int[] getColors() {

        return new int[]{Color.rgb(150, 150, 220), Color.rgb(150, 220, 140)};


//        int stacksize = 2;
//
//        // have as many colors as stack-values per entry
//        int[] colors = new int[stacksize];
//
//        for (int i = 0; i < stacksize; i++) {
//            colors[i] = ColorTemplate.VORDIPLOM_COLORS[i];
//        }
//
//        return colors;
    }

    @Override
    public void onResume() {
        super.onResume();
        drawChart();
    }
}
