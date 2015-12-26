package com.shenghua.battery.chart;

import android.content.Context;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.shenghua.battery.R;

import java.text.DecimalFormat;

/**
 * Created by shenghua on 12/25/15.
 */
public class LogYAxisValueFormatter implements YAxisValueFormatter {

    private DecimalFormat mFormat;
    private String mMeasureUnit;

    public static LogYAxisValueFormatter createPowerAxisValueFormatter() {
        return new LogYAxisValueFormatter("##0", "");
        //return new LogYAxisValueFormatter("##0", "%");
    }

    public static LogYAxisValueFormatter createRateAxisValueFormatter(Context context) {
        return new LogYAxisValueFormatter("##0.00", "");
        //return new LogYAxisValueFormatter("###,##0.0", context.getString(R.string.chart_charge_rate_unit));
    }

    public static LogYAxisValueFormatter createDurationAxisValueFormatter(Context context) {
        return new LogYAxisValueFormatter("###,##0.0", "");
        //return new LogYAxisValueFormatter("###,##0.0", context.getString(R.string.chart_charge_duration_unit));
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
