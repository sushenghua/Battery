package com.shenghua.battery.chart;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by shenghua on 12/25/15.
 */
public class LogYAxisValueFormatter implements YAxisValueFormatter {

    private DecimalFormat mFormat;
    private String mMeasureUnit;

    public static LogYAxisValueFormatter createPowerAxisValueFormatter() {
        return new LogYAxisValueFormatter("##0", "%");
    }

    public static LogYAxisValueFormatter createDurationAxisValueFormatter() {
        return new LogYAxisValueFormatter("###,##0.0", "min");
    }

    public LogYAxisValueFormatter(String format, String measureUnit) {
        mFormat = new DecimalFormat(format);
        mMeasureUnit = measureUnit;
    }

    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
        return mFormat.format(value) + mMeasureUnit;
    }
}
