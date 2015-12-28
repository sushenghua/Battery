package com.shenghua.battery.chart;

import android.content.Context;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.shenghua.battery.R;

import java.text.DecimalFormat;

/**
 * Created by shenghua on 12/25/15.
 */
public class LogValueFormatter implements ValueFormatter {

    private DecimalFormat mFormat;
    private String mMeasureUnit;

    public static LogValueFormatter createPowerValueFormatter() {
        return new LogValueFormatter("##0", "%");
    }

    public static LogValueFormatter createRateValueFormatter(Context context) {
        return new LogValueFormatter("###,##0.00", "");
        //return new LogValueFormatter("###,##0.00", context.getString(R.string.chart_charge_rate_unit));
    }

    public static LogValueFormatter createDurationValueFormatter(Context context) {
        return new LogValueFormatter("###,##0.0", context.getString(R.string.chart_charge_duration_unit));
    }

    public LogValueFormatter(String format, String measureUnit) {
        mFormat = new DecimalFormat(format);
        mMeasureUnit = measureUnit;
    }
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value) + mMeasureUnit;
    }
}
