package com.shenghua.battery.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.renderer.CombinedChartRenderer;

/**
 * Created by shenghua on 12/25/15.
 */
public class BatteryCombinedChart extends CombinedChart {

    public BatteryCombinedChart(Context context) {
        super(context);
    }

    public BatteryCombinedChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BatteryCombinedChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setData(CombinedData data) {
        mData = null;
        mRenderer = null;
        super.setData(data);
        mRenderer = new BatteryCombinedChartRenderer(this, mAnimator, mViewPortHandler);
        mRenderer.initBuffers();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mXAxisRenderer.renderAxisLine(canvas);
    }
}
