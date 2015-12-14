package com.miirr.shenghua.batterylog;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BatteryFrontView extends View {

    public static final int POLE_DEFAULT_WIDTH = 512;
    public static final int LIQUID_COVER_DEFAULT_HEIGHT = 522;
    public static final int LIQUID_COVER_DEFAULT_H_MARGIN = 6;
    public static final int LIQUID_COVER_DEFAULT_V_MARGIN = 2;
    public static final int POLE_TOP_HEIGHT = 50;
    public static final int POLE_TOP_OVERLAY = 10;

    // drawable parameters and default values
    private float mScale = 1.0f;
    private int mPoleTopWidth = 256;
    private int mPoleWidth = POLE_DEFAULT_WIDTH;
    private int mLiquidCoverHeight = LIQUID_COVER_DEFAULT_HEIGHT;
    private int mLiquidCoverHMargin = LIQUID_COVER_DEFAULT_H_MARGIN;
    private int mLiquidCoverVMargin = LIQUID_COVER_DEFAULT_V_MARGIN;

    private int mEntireHeight;

    private BitmapDrawable mBatteryPositivePoleTop;
    private BitmapDrawable mBatteryPositivePoleBottom;
    private BitmapDrawable mBatteryLiquidCover;
    private BitmapDrawable mBatteryNegativePole;
    private LayerDrawable mBatteryLayerDrawable;

    public BatteryFrontView(Context context) {
        super(context);
        init(null, 0);
    }

    public BatteryFrontView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BatteryFrontView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private Bitmap flipImage(Bitmap src) {
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BatteryView, defStyle, 0);
        try {
            mScale = a.getFloat(R.styleable.BatteryView_scale, mScale);
            mPoleTopWidth = a.getInt(
                    R.styleable.BatteryView_poleTopWidth, mPoleTopWidth);
            mPoleWidth = a.getInt(
                    R.styleable.BatteryView_poleWidth, mPoleWidth);
            mLiquidCoverHeight = a.getInt(
                    R.styleable.BatteryView_liquidCoverHeight, mLiquidCoverHeight);
            mLiquidCoverHMargin = a.getInt(
                    R.styleable.BatteryView_liquidCoverHMargin, mLiquidCoverHMargin);
            mLiquidCoverVMargin = a.getInt(
                    R.styleable.BatteryView_liquidCoverVMargin, mLiquidCoverVMargin);
        } finally {
            a.recycle();
        }

        // bitmap
        ImageSize poleImgSize = ImageSize.getBitmapSize(getResources(), R.drawable.battery_base);
        mEntireHeight = POLE_TOP_HEIGHT - POLE_TOP_OVERLAY + 2 * mLiquidCoverVMargin +
                mLiquidCoverHeight + 2 * poleImgSize.height;
                //Log.d("------------------>", getBitmapSize(R.drawable.battery_base).toString());

        Bitmap bitmapOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.battery_base);
        Bitmap bitmap = flipImage(bitmapOriginal);

        // create bitmap drawables
        mBatteryPositivePoleTop = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmap, mPoleTopWidth, POLE_TOP_HEIGHT, true));
        mBatteryPositivePoleBottom = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmap, mPoleWidth, poleImgSize.height, true));
        Bitmap liquidCoverBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.battery_liquid_cover);
        mBatteryLiquidCover = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(
                        liquidCoverBitmap,
                        mPoleWidth - 2 * mLiquidCoverHMargin,
                        mLiquidCoverHeight, true)
        );
        mBatteryNegativePole = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(bitmapOriginal, mPoleWidth, poleImgSize.height, true));

        // compose multiple layers
        mBatteryLayerDrawable = new LayerDrawable(new Drawable[]{
                mBatteryPositivePoleTop,
                mBatteryPositivePoleBottom,
                mBatteryLiquidCover,
                mBatteryNegativePole});

        // apply scale
        setScaleX(mScale);
        setScaleY(mScale);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // decide center
        boolean inPortraitOrientation = w < h;
        int cX = w / 2;
        int cY = h / 2;

        // positive pole top
        int r = mPoleTopWidth / 2;
        int t = cY - mEntireHeight / 2;
        int b = t + POLE_TOP_HEIGHT;
        mBatteryPositivePoleTop.setBounds(cX - r, t, cX + r, b);

        // positive pole bottom
        r = mPoleWidth / 2;
        t = t + POLE_TOP_HEIGHT - POLE_TOP_OVERLAY;
        b = t + mBatteryPositivePoleBottom.getIntrinsicHeight();
        mBatteryPositivePoleBottom.setBounds(cX - r, t, cX + r, b);

        // liquid cover
        r = mPoleWidth / 2 - mLiquidCoverHMargin;
        t = b + mLiquidCoverVMargin;
        b = t + mLiquidCoverHeight;
        mBatteryLiquidCover.setBounds(cX - r, t, cX + r, b);

        // negative pole
        r = mPoleWidth / 2;
        t = b + mLiquidCoverVMargin;
        b = t + mBatteryNegativePole.getIntrinsicHeight();
        mBatteryNegativePole.setBounds(cX - r, t, cX + r, b);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("screen size: ", getWidth() + ", " + getHeight());
        mBatteryLayerDrawable.draw(canvas);
    }

    public int getPoleTopWidth() {
        return mPoleTopWidth;
    }

    public void setPoleTopWidth(int width) {
        mPoleTopWidth = width;
        invalidateDrawable(mBatteryPositivePoleTop);
    }

    public int getPoleWidth() {
        return mPoleWidth;
    }

    public void setmPoleWidth(int width) {
        mPoleWidth = width;
        invalidateDrawable(mBatteryPositivePoleBottom);
        invalidateDrawable(mBatteryLiquidCover);
        invalidateDrawable(mBatteryNegativePole);
    }

    public int getLiquidCoverHeight() {
        return mLiquidCoverHeight;
    }

    public void setLiquidCoverHeight(int height) {
        mLiquidCoverHeight = height;
        invalidateDrawable(mBatteryLiquidCover);
        invalidateDrawable(mBatteryNegativePole);
    }

    public int getLiquidCoverHMargin() {
        return mLiquidCoverHMargin;
    }

    public void setLiquidCoverHMargin(int margin) {
        mLiquidCoverHMargin = margin;
        invalidateDrawable(mBatteryLiquidCover);
    }

    public int getLiquidCoverVMargin() {
        return mLiquidCoverVMargin;
    }

    public void setLiquidCoverVMargin(int margin) {
        mLiquidCoverVMargin = margin;
        invalidateDrawable(mBatteryLiquidCover);
        invalidateDrawable(mBatteryNegativePole);
    }
}
