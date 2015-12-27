package com.shenghua.battery.chart;

import android.graphics.Canvas;

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

                        dvfp_cache.posX = valuePoints[j];
                        dvfp_cache.posY = valuePoints[j + 1] + (val >= 0 ? posOffset : negOffset);
                        dvfp_cache.value = val;
                        dvfp_cache.drawTextHeight = valueTextHeight;
                        dvfp_cache.drawTextMarginV = valueOffsetPlus;
                        dvfp_cache.baseRectIndex = j * 2;
                        dvfp_cache.firstRectIndexOffset = 0;
                        dvfp_cache.lastRectIndexOffset = 0;
                        dvfp_cache.forceAdditionalCheck = true;

                        drawValue(dvfp_cache, entry, dataSet, i, c);

                        if (dvfp_cache.returnFlag == DrawValueFuncParams.NEED_BREAK)
                            break;
                        else if (dvfp_cache.returnFlag == DrawValueFuncParams.NEED_CONTINUE)
                            continue;
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

                            dvfp_cache.previousIndicatorCount = 0;
                            dvfp_cache.prePosValuePosY = 0;
                            dvfp_cache.preNegValuePosY = 0;

                            for (int k = 0; k < transformed.length; k += 2) {

                                dvfp_cache.posX = valuePoints[j];
                                dvfp_cache.posY = transformed[k + 1] + (vals[k/2] >= 0 ? posOffset : negOffset);
                                dvfp_cache.value = vals[k/2];
                                dvfp_cache.drawTextHeight = valueTextHeight;
                                dvfp_cache.drawTextMarginV = valueOffsetPlus;
                                dvfp_cache.baseRectIndex = j * transformed.length; // j/2 * transformed.length/2 * 4
                                dvfp_cache.firstRectIndexOffset = k / 2;
                                dvfp_cache.lastRectIndexOffset = transformed.length / 2 - 1;
                                dvfp_cache.forceAdditionalCheck = false;

                                drawValue(dvfp_cache, entry, dataSet, i, c);

                                if (dvfp_cache.returnFlag == DrawValueFuncParams.NEED_BREAK)
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawValue(DrawValueFuncParams dvfp,
                           BarEntry entry, BarDataSet dataSet, int dataSetIndex, Canvas c) {

        String drawText = dataSet.getValueFormatter().getFormattedValue(dvfp.value, entry, dataSetIndex, mViewPortHandler);
        float drawTextWidth = Utils.calcTextWidth(mValuePaint, drawText);
        vCollisionDetect(
                dvfp.posY,
                dvfp.prePosValuePosY,
                dvfp.preNegValuePosY,
                dvfp.forceAdditionalCheck,
                dvfp.previousIndicatorCount,
                dvfp.value >= 0f,
                drawTextWidth,
                dvfp.drawTextHeight,
                dvfp.drawTextMarginV,
                dvfp.baseRectIndex,
                dvfp.firstRectIndexOffset,
                dvfp.lastRectIndexOffset,
                mBarBuffers[dataSetIndex].buffer);
        if (mCD.hasCollision()) {
            dvfp.posY = mCD.adjustedY;
        }
        if (dvfp.value >= 0f)
            dvfp.prePosValuePosY = dvfp.posY;
        else
            dvfp.preNegValuePosY = dvfp.posY;

        dvfp.returnFlag = DrawValueFuncParams.FINE;

        if (!mViewPortHandler.isInBoundsRight(dvfp.posX + drawTextWidth * .5f)) {
            dvfp.returnFlag = DrawValueFuncParams.NEED_BREAK;
            return;
        }

        if (!mViewPortHandler.isInBoundsY(dvfp.posY)
                || !mViewPortHandler.isInBoundsLeft(dvfp.posX - drawTextWidth * .5f)) {
            dvfp.returnFlag = DrawValueFuncParams.NEED_CONTINUE;
            return;
        }

        if (mCD.needDrawIndicatorLine
                && mViewPortHandler.isInBoundsTop(mCD.indicatorLinePoints[3])
                && mViewPortHandler.isInBoundsBottom(mCD.indicatorLinePoints[1])
                && mViewPortHandler.isInBoundsLeft(mCD.indicatorLinePoints[0])
                && mViewPortHandler.isInBoundsRight(mCD.indicatorLinePoints[5])) {
            ++dvfp.previousIndicatorCount;
            mValuePaint.setStrokeWidth(2);
            c.drawLines(mCD.indicatorLinePoints, mValuePaint);
        }
        c.drawText(drawText, dvfp.posX, dvfp.posY, mValuePaint);
    }

    private class DrawValueFuncParams {
        float value;
        float posX;
        float posY;
        float drawTextHeight;
        float drawTextMarginV;
        int baseRectIndex;
        int firstRectIndexOffset;
        int lastRectIndexOffset;
        boolean forceAdditionalCheck;
        int previousIndicatorCount;
        float prePosValuePosY;
        float preNegValuePosY;
        //float[] preNegValuePosYs = new float[5];

        int returnFlag;
        static final int FINE = 0;
        static final int NEED_BREAK = 1;
        static final int NEED_CONTINUE = 2;
    }
    private DrawValueFuncParams dvfp_cache = new DrawValueFuncParams();

    private CollisionData vCollisionDetect(float y,
                                           float prePosValueY,
                                           float preNegValueY,
                                           boolean forceValueAddintionalCheck,
                                           int previousIndicatorCount,
                                           boolean drawPositive,
                                           float textDrawWidth,
                                           float textDrawHeight,
                                           float textDrawMarginV,
                                           int baseRectIndex,
                                           int curRectIndexOffset,
                                           int lastRectIndexOffset,
                                           float[] rectBuffer) {
        // clear collision cache data
        mCD.clear();

        float minDistanceToOthers = textDrawHeight;// + .3f * textDrawMarginV;
        float rectCollisionAdjustment = textDrawHeight + .4f * textDrawMarginV;
        float textCollisionAdjustment = textDrawHeight * 1.5f + textDrawMarginV;
        mCD.adjustedY = drawPositive? y : y - .5f*textDrawHeight; // save y for rect collision check

        // check if collided with base(previous) value text, base rect no need to check
        if (curRectIndexOffset > 0) {
            if (drawPositive && prePosValueY - y < minDistanceToOthers) { // must above and keep min distance
                mCD.collisionPos = CollisionData.CollidedPreviousValueText;
                mCD.adjustedY = prePosValueY - textCollisionAdjustment;
            }
            else if (!drawPositive && Math.abs(y - preNegValueY) < minDistanceToOthers) {
                mCD.collisionPos = CollisionData.CollidedPreviousValueText;
                mCD.adjustedY = preNegValueY - textCollisionAdjustment;
            }
        }

        // check if collided with bars (self bar and succeeding bars)
        for (int i = curRectIndexOffset; i <= lastRectIndexOffset; ++i) {

            // temporary check
            int collision = CollisionData.NoCollision;

            if (Math.abs(mCD.adjustedY - rectBuffer[baseRectIndex + i*4 + 3]) < minDistanceToOthers) {
                //Log.d("--->collied", "rect:"+i+", bottom edge:"+rectBuffer[baseRectIndex + i*4 + 3]);
                collision |= CollisionData.CollidedBottom;
            }
            if (Math.abs(mCD.adjustedY - rectBuffer[baseRectIndex + i*4 + 1]) < minDistanceToOthers) {
                //Log.d("--->collied", "rect:"+i+", top edge:"+rectBuffer[baseRectIndex + i*4 + 1]);
                collision |= CollisionData.CollidedTop;
            }
            if (forceValueAddintionalCheck && i == curRectIndexOffset) {
                if (drawPositive && mCD.adjustedY - rectBuffer[baseRectIndex + i*4 + 3] > -minDistanceToOthers) {
                    collision |= CollisionData.CollidedBottom;
                }
                else if (mCD.adjustedY - rectBuffer[baseRectIndex + i*4 + 1] < minDistanceToOthers) {
                    collision |= CollisionData.CollidedTop;
                }
            }
            if (mCD.hasRectCollision(collision)) {
                mCD.recentCollisionRectIndex = i;
                if (drawPositive)
                    mCD.adjustedY = rectBuffer[baseRectIndex + i * 4 + 1] - rectCollisionAdjustment;
                else {
                    mCD.adjustedY = rectBuffer[baseRectIndex + i * 4 + 3] - rectCollisionAdjustment;
                    if (Math.abs(mCD.adjustedY - preNegValueY) < minDistanceToOthers)
                        mCD.adjustedY = preNegValueY - textCollisionAdjustment;
                }

                // save the collision check result
                mCD.addCollision(collision);
            }
        }

        int curRectIndex = baseRectIndex + curRectIndexOffset * 4;
        mCD.needDrawIndicatorLine = (mCD.adjustedY < rectBuffer[curRectIndex + 1])
                || (mCD.adjustedY > rectBuffer[curRectIndex + 3]);

        if (mCD.needDrawIndicatorLine) {

            // set indicator line points
            float rectWidth = rectBuffer[baseRectIndex + 2] - rectBuffer[baseRectIndex];
            float indicatorLineTopY = mCD.adjustedY + textDrawHeight * 0.4f;
            mCD.indicatorLinePoints[2] = rectBuffer[baseRectIndex] + rectWidth * .5f - textDrawWidth * .5f - 5;
            mCD.indicatorLinePoints[3] = indicatorLineTopY;
            mCD.indicatorLinePoints[4] = mCD.indicatorLinePoints[2];
            mCD.indicatorLinePoints[5] = mCD.indicatorLinePoints[3];
            mCD.indicatorLinePoints[6] = rectBuffer[baseRectIndex] + rectWidth * .5f + textDrawWidth * .5f + 5;
            mCD.indicatorLinePoints[7] = indicatorLineTopY;

            float space = rectWidth * .5f - textDrawWidth * 0.8f;
            space = space < 0? 0 : space;
            float indicatorLineLeftX = rectBuffer[curRectIndex] + space;
            indicatorLineLeftX -= previousIndicatorCount * space / (lastRectIndexOffset + 1);//rectWidth * 0.1f;
            float leftMost = rectBuffer[curRectIndex] + 5f;//rectWidth * .1f;
            indicatorLineLeftX = indicatorLineLeftX < leftMost ? leftMost : indicatorLineLeftX;
            float selfRectHeight = rectBuffer[curRectIndex + 3] - rectBuffer[curRectIndex + 1];
            mCD.indicatorLinePoints[0] = indicatorLineLeftX;
            if (drawPositive)
                mCD.indicatorLinePoints[1] = selfRectHeight > minDistanceToOthers ?
                        rectBuffer[curRectIndex + 1] + minDistanceToOthers / 2
                        : rectBuffer[curRectIndex + 1] + selfRectHeight / 2;
            else
                mCD.indicatorLinePoints[1] = selfRectHeight > minDistanceToOthers ?
                        rectBuffer[curRectIndex + 3] - minDistanceToOthers / 2
                        : rectBuffer[curRectIndex + 3] - selfRectHeight / 2;
        }

        return mCD;
    }

    private class CollisionData {
        public static final int NoCollision = 0;
        public static final int CollidedTop = 1;
        public static final int CollidedBottom = 2;
        public static final int CollidedTopBottom = 3;
        public static final int CollidedPreviousValueText = 4;

        public int collisionPos;
        public int recentCollisionRectIndex;
        public float adjustedY;
        public boolean needDrawIndicatorLine;
        public float[] indicatorLinePoints = new float[8];

        public void addCollision(int collisionPosition) {
            collisionPos |= collisionPosition;
        }

        public boolean hasCollision() {
            return collisionPos != NoCollision;
        }
        public boolean hasRectCollision() {
            return (collisionPos & CollidedTopBottom) != NoCollision;
        }

        public boolean hasRectCollision(int collisionPosition) {
            return (collisionPosition & CollidedTopBottom) != NoCollision;
        }

        public void clear() {
            collisionPos = NoCollision;
            recentCollisionRectIndex = -1;
            needDrawIndicatorLine = false;
            adjustedY = 0;
        }
    }

    private CollisionData mCD = new CollisionData();
}
