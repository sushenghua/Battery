package com.miirr.shenghua.batterylog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;

import java.lang.ref.WeakReference;

public class BatteryFrontView extends View {
    // appearance params cached from parent view (BatteryView)
    private int centerX;
    private int centerY;

    private int poleTopWidth;
    private int poleWidth;
    private int poleHeight;
    private int liquidCoverHMargin;
    private int liquidCoverVMargin;
    private int liquidCoverWidth;
    private int liquidCoverHeight;
    private int batteryHeight;

    // drawables
    private BitmapDrawable batteryPositivePoleTop;
    private BitmapDrawable batteryPositivePoleBottom;
    private BitmapDrawable batteryLiquidCover;
    private BitmapDrawable batteryNegativePole;
    private LayerDrawable batteryLayerDrawable;

    // weak ref to parent view
    private WeakReference<BatteryView> parentView;

    public BatteryFrontView(Context context, BatteryView parentView) {
        super(context);
        this.parentView = new WeakReference<>(parentView);
        init();
    }

    private Bitmap flipImage(Bitmap src) {
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
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
        centerY = parentView.getCenterY();
        poleTopWidth = parentView.getPoleTopWidth();
        poleWidth = parentView.getPoleWidth();
        poleHeight = parentView.getPoleHeight();
        liquidCoverHMargin = parentView.getLiquidCoverHMargin();
        liquidCoverVMargin = parentView.getLiquidCoverVMargin();
        liquidCoverWidth = parentView.getLiquidCoverWidth();
        liquidCoverHeight = parentView.getLiquidCoverHeight();
        batteryHeight = parentView.getBatteryHeight();

        return true;
    }

    private void init() {
        // load appearance params
        if (!updateCachedParams())
            return;

        // bitmap
        Bitmap bitmapOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.battery_base);
        Bitmap bitmap = flipImage(bitmapOriginal);

        // create bitmap drawables
        batteryPositivePoleTop = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmap, poleTopWidth, BatteryView.POLE_TOP_HEIGHT, true));
        batteryPositivePoleBottom = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmap, poleWidth, poleHeight, true));
        Bitmap liquidCoverBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.battery_liquid_cover);
        batteryLiquidCover = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(
                        liquidCoverBitmap,
                        liquidCoverWidth,
                        liquidCoverHeight, true)
        );
        batteryNegativePole = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmapOriginal, poleWidth, poleHeight, true));

        // compose multiple layers
        batteryLayerDrawable = new LayerDrawable(new Drawable[]{
                batteryPositivePoleTop,
                batteryPositivePoleBottom,
                batteryLiquidCover,
                batteryNegativePole});
    }

    private void updateBounds() {
        // positive pole top
        int r = poleTopWidth / 2;
        int t = centerY - batteryHeight / 2;
        int b = t + BatteryView.POLE_TOP_HEIGHT;
        batteryPositivePoleTop.setBounds(centerX - r, t, centerX + r, b);

        // positive pole bottom
        r = poleWidth / 2;
        t = t + BatteryView.POLE_TOP_HEIGHT - BatteryView.POLE_TOP_OVERLAY;
        b = t + batteryPositivePoleBottom.getIntrinsicHeight();
        batteryPositivePoleBottom.setBounds(centerX - r, t, centerX + r, b);

        // liquid cover
        r = poleWidth / 2 - liquidCoverHMargin;
        t = b + liquidCoverVMargin;
        b = t + liquidCoverHeight;
        batteryLiquidCover.setBounds(centerX - r, t, centerX + r, b);

        // negative pole
        r = poleWidth / 2;
        t = b + liquidCoverVMargin;
        b = t + batteryNegativePole.getIntrinsicHeight();
        batteryNegativePole.setBounds(centerX - r, t, centerX + r, b);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //Log.d("BatteryFrontView", "onSizeChanged");
        if (updateCachedParams())
            updateBounds();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("screen size: ", getWidth() + ", " + getHeight());
        batteryLayerDrawable.draw(canvas);
    }
}
