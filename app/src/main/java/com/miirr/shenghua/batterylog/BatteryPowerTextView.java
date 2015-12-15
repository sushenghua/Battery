package com.miirr.shenghua.batterylog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.View;

import java.lang.ref.WeakReference;

public class BatteryPowerTextView extends View {

    private static final int TEXT_SIZE = 100;

    private String textValue="";
    private float textWidth;
    private float textHeight;

    // render
    private int centerX;
    private int centerY;
    private TextPaint paint;

    // weak ref to parent view
    private WeakReference<BatteryView> parentView;

    public BatteryPowerTextView(Context context, BatteryView parentView) {
        super(context);
        this.parentView = new WeakReference<>(parentView);
        init();
    }

    private void init() {

        // Set up a default TextPaint object
        paint = new TextPaint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);

        paint.setTextSize(TEXT_SIZE);
        paint.setColor(Color.WHITE);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private boolean updateCachedParams() {
        // get parent view
        if (this.parentView == null)
            return false;
        BatteryView parentView = this.parentView.get();
        if (parentView == null)
            return false;

        // cache paras from parent view
        centerX = parentView.getCenterX();
        centerY = parentView.getCenterY() + TEXT_SIZE / 2;

        return true;
    }

    private void invalidateTextPaintAndMeasurements() {
        textWidth = paint.measureText(textValue);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        textHeight = fontMetrics.bottom;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateCachedParams();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(textValue, centerX-textWidth/2, centerY-textHeight/2, paint);
    }

    public void setPower(int p) {
        textValue = p + "%";
        invalidateTextPaintAndMeasurements();
    }
}
