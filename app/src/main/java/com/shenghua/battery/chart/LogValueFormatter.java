package com.shenghua.battery.chart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

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

    public static LogValueFormatter createDurationValueFormatter() {
        return new LogValueFormatter("###,##0.0", "min");
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
