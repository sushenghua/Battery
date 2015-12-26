package com.shenghua.battery.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.BarDataProvider;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

/**
 * Created by shenghua on 12/25/15.
 */
public class BatteryBarChartRenderer extends BarChartRenderer {
    public BatteryBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    public void drawData(Canvas c) {

        BarData barData = mChart.getBarData();
//Log.d("--->render", "data set count:"+barData.getDataSetCount());
        for (int i = 0; i < barData.getDataSetCount(); i++) {

            BarDataSet set = barData.getDataSetByIndex(i);

            if (set.isVisible() && set.getEntryCount() > 0) {
                drawDataSet(c, set, i);
            }
        }
    }

    @Override
    protected void drawDataSet(Canvas c, BarDataSet dataSet, int index) {

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mShadowPaint.setColor(dataSet.getBarShadowColor());

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        List<BarEntry> entries = dataSet.getYVals();

        // initialize the buffer
        BarBuffer buffer = mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
        buffer.setBarSpace(dataSet.getBarSpace());
        buffer.setDataSet(index);
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));

        buffer.feed(entries);

        trans.pointValuesToPixel(buffer.buffer);

        // if multiple colors
        if (dataSet.getColors().size() > 1) {
//Log.d("--->render", "buffer size:" + buffer.size());
            for (int j = 0; j < buffer.size(); j += 4) {

                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;

                if (mChart.isDrawBarShadowEnabled()) {
                    c.drawRect(buffer.buffer[j], mViewPortHandler.contentTop(),
                            buffer.buffer[j + 2],
                            mViewPortHandler.contentBottom(), mShadowPaint);
                }

                // Set the color for the currently drawn value. If the index
                // is
                // out of bounds, reuse colors.
                mRenderPaint.setColor(dataSet.getColor(j / 4));
                c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], mRenderPaint);
//Log.d("--->render", "rect: ("+buffer.buffer[j]+", "+buffer.buffer[j + 1]+", "+ buffer.buffer[j + 2]+", "+buffer.buffer[j + 3]+")");
            }
        } else {

            mRenderPaint.setColor(dataSet.getColor());

            for (int j = 0; j < buffer.size(); j += 4) {

                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;

                if (mChart.isDrawBarShadowEnabled()) {
                    c.drawRect(buffer.buffer[j], mViewPortHandler.contentTop(),
                            buffer.buffer[j + 2],
                            mViewPortHandler.contentBottom(), mShadowPaint);
                }

                c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], mRenderPaint);
            }
        }
    }

    @Override
    public void drawValues(Canvas c) {
        // if values are drawn
        if (passesCheck()) {

            List<BarDataSet> dataSets = mChart.getBarData().getDataSets();

            final float valueOffsetPlus = Utils.convertDpToPixel(4.5f);
            float posOffset = 0f;
            float negOffset = 0f;
            boolean drawValueAboveBar = mChart.isDrawValueAboveBarEnabled();

            for (int i = 0; i < mChart.getBarData().getDataSetCount(); i++) {

                BarDataSet dataSet = dataSets.get(i);

                if (!dataSet.isDrawValuesEnabled() || dataSet.getEntryCount() == 0)
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                boolean isInverted = mChart.isInverted(dataSet.getAxisDependency());

                // calculate the correct offset depending on the draw position of
                // the value
                float valueTextHeight = Utils.calcTextHeight(mValuePaint, "8");
                posOffset = (drawValueAboveBar ? -valueOffsetPlus : valueTextHeight + valueOffsetPlus);
                negOffset = (drawValueAboveBar ? valueTextHeight + valueOffsetPlus : -valueOffsetPlus);

                if (isInverted) {
                    posOffset = -posOffset - valueTextHeight;
                    negOffset = -negOffset - valueTextHeight;
                }

                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                List<BarEntry> entries = dataSet.getYVals();

                float[] valuePoints = getTransformedValues(trans, entries, i);

                // if only single values are drawn (sum)
                if (!dataSet.isStacked()) {

                    for (int j = 0; j < valuePoints.length * mAnimator.getPhaseX(); j += 2) {

                        if (!mViewPortHandler.isInBoundsRight(valuePoints[j]))
                            break;

                        if (!mViewPortHandler.isInBoundsY(valuePoints[j + 1])
                                || !mViewPortHandler.isInBoundsLeft(valuePoints[j]))
                            continue;

                        BarEntry entry = entries.get(j / 2);
                        float val = entry.getVal();

                        drawValue(c, dataSet.getValueFormatter(), val, entry, i, valuePoints[j],
                                valuePoints[j + 1] + (val >= 0 ? posOffset : negOffset));
                    }

                    // if we have stacks
                } else {

                    for (int j = 0; j < (valuePoints.length - 1) * mAnimator.getPhaseX(); j += 2) {

                        BarEntry entry = entries.get(j / 2);

                        float[] vals = entry.getVals();
//Log.d("--->render", "j:"+j+" vals len: "+vals.length);

                        // we still draw stacked bars, but there is one
                        // non-stacked
                        // in between
                        if (vals == null) {

                            if (!mViewPortHandler.isInBoundsRight(valuePoints[j]))
                                break;

                            if (!mViewPortHandler.isInBoundsY(valuePoints[j + 1])
                                    || !mViewPortHandler.isInBoundsLeft(valuePoints[j]))
                                continue;

                            drawValue(c, dataSet.getValueFormatter(), entry.getVal(), entry, i, valuePoints[j],
                                    valuePoints[j + 1] + (entry.getVal() >= 0 ? posOffset : negOffset));

                            // draw stack values
                        } else {

                            float[] transformed = new float[vals.length * 2];

                            float posY = 0f;
                            float negY = -entry.getNegativeSum();

                            for (int k = 0, idx = 0; k < transformed.length; k += 2, idx++) {

                                float value = vals[idx];
//Log.d("--->value", "k:"+k+" value:"+value);
                                float y;

                                if (value >= 0f) {
                                    posY += value;
                                    y = posY;
                                } else {
                                    y = negY;
                                    negY -= value;
                                }

                                transformed[k + 1] = y * mAnimator.getPhaseY();
                            }

                            trans.pointValuesToPixel(transformed);

                            float lowerBarY = 0;

                            for (int k = 0; k < transformed.length; k += 2) {

                                float x = valuePoints[j];

                                int rectIndex = (j + k / 2) * 4;
//Log.d("--->rect", "top:"+mBarBuffers[i].buffer[rectIndex+1]+", bottom:"+mBarBuffers[i].buffer[rectIndex+3]);
                                float rectHeight = mBarBuffers[i].buffer[rectIndex+3] - mBarBuffers[i].buffer[rectIndex+1];
                                float finalPosOffset = posOffset;
                                float finalNegOffset = negOffset;
                                boolean needShift = (rectHeight < valueTextHeight + valueOffsetPlus * 1.5 && !drawValueAboveBar);
                                if (needShift) {
                                    finalPosOffset -= valueTextHeight + 2 * valueOffsetPlus;
                                    finalNegOffset += valueTextHeight + 2 * valueOffsetPlus;
                                }
                                float y = transformed[k + 1]
                                        + (vals[k / 2] >= 0 ? finalPosOffset : finalNegOffset);

                                if (Math.abs(y - lowerBarY) < valueTextHeight * 1.5) {
                                    if (y >= 0) {
                                        y -= valueTextHeight * 1.5;
                                        float deltaToBarTop = y - mBarBuffers[i].buffer[rectIndex + 1];
                                        if (deltaToBarTop > 0 && deltaToBarTop < valueOffsetPlus) {
                                            needShift = true;
                                            y -= deltaToBarTop + valueTextHeight + valueOffsetPlus;
                                        }
                                    }
                                    else {
                                        y += valueTextHeight * 1.5;
                                        float deltaToBarBottom = y - mBarBuffers[i].buffer[rectIndex + 3];
                                        if (deltaToBarBottom < 0 && deltaToBarBottom > -valueOffsetPlus) {
                                            needShift = true;
                                            y += deltaToBarBottom + valueTextHeight + valueOffsetPlus;
                                        }
                                    }
                                }
                                lowerBarY = y;

                                String drawText = dataSet.getValueFormatter().getFormattedValue(vals[k/2], entry, i, mViewPortHandler);
                                float txtWidth = Utils.calcTextWidth(mValuePaint, drawText);

                                if (!mViewPortHandler.isInBoundsRight(x + txtWidth*0.5f))
                                    break;

                                if (!mViewPortHandler.isInBoundsY(y)
                                        || !mViewPortHandler.isInBoundsLeft(x-txtWidth*0.5f))
                                    continue;

                                float rectWidth = mBarBuffers[i].buffer[rectIndex+2] -mBarBuffers[i].buffer[rectIndex];
                                float startX = mBarBuffers[i].buffer[rectIndex]+ rectWidth*.5f - txtWidth*1.6f;
                                float endX = mBarBuffers[i].buffer[rectIndex]+ rectWidth*.5f + txtWidth*.75f;
                                if (needShift && mViewPortHandler.isInBoundsLeft(startX)
                                        && mViewPortHandler.isInBoundsRight(endX)) {

//                                    float hLineY = vals[k / 2] >= 0 ? mBarBuffers[i].buffer[rectIndex+1] - valueOffsetPlus/2
//                                            : mBarBuffers[i].buffer[rectIndex+3] + finalNegOffset + valueOffsetPlus/2;
                                    float hLineY =  y + valueTextHeight * 0.3f;

                                    mValuePaint.setAntiAlias(true);
                                    mValuePaint.setStrokeWidth(2);

                                    float startXLeftMost =  mBarBuffers[i].buffer[rectIndex]+ rectWidth*.1f;
                                    startX = startX < startXLeftMost? startXLeftMost : startX;
                                    c.drawLine( startX,
                                                mBarBuffers[i].buffer[rectIndex+3] - rectHeight/2,
                                                mBarBuffers[i].buffer[rectIndex] + rectWidth*.5f - txtWidth,
                                                hLineY,
                                                mValuePaint
                                    );
                                    c.drawLine( mBarBuffers[i].buffer[rectIndex] + rectWidth*.5f - txtWidth,
                                                hLineY,
                                                endX,
                                                hLineY,
                                                mValuePaint
                                    );
                                }
                                c.drawText(drawText, x, y, mValuePaint);
//                                drawValue(c, dataSet.getValueFormatter(), vals[k / 2], entry, i, x, y);
                            }
                        }
                    }
                }
            }
        }
    }
}
