package com.shenghua.battery;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * TODO: document your custom view class.
 */
public class BatteryView extends FrameLayout {

    public static final int POLE_TOP_HEIGHT = 50;
    public static final int POLE_TOP_OVERLAY = 10;
    private static final int POLE_TOP_DEFAULT_WIDTH = 256;
    private static final int POLE_DEFAULT_WIDTH = 512;
    private static final int POLE_DEAULT_HEIGHT = 64;
    private static final int LIQUID_COVER_DEFAULT_HEIGHT = 550;
    private static final int LIQUID_COVER_DEFAULT_H_MARGIN = 8;
    private static final int LIQUID_COVER_DEFAULT_V_MARGIN = 2;
    private static final int LIQUID_DEFAULT_H_MARGIN = 16;
    private static final int LIQUID_DEFAULT_V_MARGIN = 4;

    // drawable parameters and default values
    private float scale = .7f;
    private int centerX;
    private int centerY;
    private int poleTopWidth = POLE_TOP_DEFAULT_WIDTH;
    private int poleWidth = POLE_DEFAULT_WIDTH;
    private int poleHeight = POLE_DEAULT_HEIGHT;
    private int liquidCoverHeight = LIQUID_COVER_DEFAULT_HEIGHT;
    private int liquidCoverHMargin = LIQUID_COVER_DEFAULT_H_MARGIN;
    private int liquidCoverVMargin = LIQUID_COVER_DEFAULT_V_MARGIN;

    private int liquidHMargin = LIQUID_DEFAULT_H_MARGIN;
    private int liquidVMargin = LIQUID_DEFAULT_V_MARGIN;

    // battery entire size
    private int batteryWidth;
    private int batteryHeight;

    // battery arg
    private int power = 0;

    // children views
    private BatteryLiquidView liquidView;
    private BatteryFrontView frontView;
    private BatteryPowerTextView textView;

    // animation
    private static final int UNIT_POWER_ANIMATION_DURATION = 20;

    public BatteryView(Context context) {
        super(context);
        init(null, 0);
    }

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void loadAttrs(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BatteryView, defStyle, 0);
        try {
            power = a.getInt(R.styleable.BatteryView_power, power);

            scale = a.getFloat(R.styleable.BatteryView_scale, scale);
            poleTopWidth = a.getInt(
                    R.styleable.BatteryView_poleTopWidth, poleTopWidth);
            poleWidth = a.getInt(
                    R.styleable.BatteryView_poleWidth, poleWidth);
            liquidCoverHeight = a.getInt(
                    R.styleable.BatteryView_liquidCoverHeight, liquidCoverHeight);
            liquidCoverHMargin = a.getInt(
                    R.styleable.BatteryView_liquidCoverHMargin, liquidCoverHMargin);
            liquidCoverVMargin = a.getInt(
                    R.styleable.BatteryView_liquidCoverVMargin, liquidCoverVMargin);

            liquidHMargin = a.getInt(
                    R.styleable.BatteryView_liquidHMargin, liquidHMargin);
            liquidVMargin = a.getInt(
                    R.styleable.BatteryView_liquidVMargin, liquidVMargin);

        } finally {
            a.recycle();
        }
    }

    private void calculatePrams() {
        //ImageSize poleImgSize = ImageSize.getBitmapSize(getResources(), R.drawable.battery_base);
        ImageSize poleImgSize = ImageSize.getBitmapSize(EmbeddedImage.getBatteryBaseImageBytes());
        batteryWidth = poleWidth; // pole is the widest block
        poleHeight = poleImgSize.height;
        batteryHeight = POLE_TOP_HEIGHT - POLE_TOP_OVERLAY + 2 * poleHeight
                + 2 * liquidCoverVMargin + liquidCoverHeight;
    }

    private void init(AttributeSet attrs, int defStyle) {

        loadAttrs(attrs, defStyle);

        calculatePrams();

        // setup children view
        liquidView = new BatteryLiquidView(getContext(), this);
        addView(liquidView);

        frontView = new BatteryFrontView(getContext(), this);
        addView(frontView);

        textView = new BatteryPowerTextView(getContext(), this);
        addView(textView);

        setScale(scale);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //Log.d("------>", "w: " + w + ", h: " + h);
        //Log.d("------>", "bw: "+batteryWidth+", bh: "+batteryHeight);
        // decide center
        centerX = w / 2;
        centerY = h / 2;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(batteryWidth, batteryHeight);
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
        liquidView.setPower(power);
        textView.setPower(power);
    }

    public void setPower(int power, boolean animated) {
        if (power < 0)
            return;

        if (animated) {
            int powerDeltaLen = Math.abs(power - this.power);
            if (powerDeltaLen > 1) {
                ObjectAnimator anim = ObjectAnimator.ofInt(this, "power", this.power, power);
                anim.setDuration(powerDeltaLen * UNIT_POWER_ANIMATION_DURATION);
                anim.setStartDelay(300);
                anim.start();
            } else {
                setPower(power);
            }
        } else {
            setPower(power);
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        setScaleX(scale);
        setScaleY(scale);
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getBatteryWidth() {
        return batteryWidth;
    }

    public int getBatteryHeight() {
        return batteryHeight;
    }

    public int getPoleTopWidth() {
        return poleTopWidth;
    }

    public void setPoleTopWidth(int poleTopWidth) {
        this.poleTopWidth = poleTopWidth;
    }

    public int getPoleWidth() {
        return poleWidth;
    }

    public void setPoleWidth(int poleWidth) {
        this.poleWidth = poleWidth;
    }

    public int getPoleHeight() {
        return poleHeight;
    }

    public int getLiquidCoverWidth() {
        return this.poleWidth - 2 * this.liquidCoverHMargin;
    }

    public int getLiquidCoverHeight() {
        return liquidCoverHeight;
    }

    public void setLiquidCoverHeight(int liquidCoverHeight) {
        this.liquidCoverHeight = liquidCoverHeight;
    }

    public int getLiquidCoverHMargin() {
        return liquidCoverHMargin;
    }

    public void setLiquidCoverHMargin(int liquidCoverHMargin) {
        this.liquidCoverHMargin = liquidCoverHMargin;
    }

    public int getLiquidCoverVMargin() {
        return liquidCoverVMargin;
    }

    public void setLiquidCoverVMargin(int liquidCoverVMargin) {
        this.liquidCoverVMargin = liquidCoverVMargin;
    }

    public int getLiquidWidth() {
        return this.poleWidth - 2 * this.liquidCoverHMargin - 2 * this.liquidHMargin;
    }

    public int getLiquidHMargin() {
        return liquidHMargin;
    }

    public void setLiquidHMargin(int liquidHMargin) {
        this.liquidHMargin = liquidHMargin;
    }

    public int getLiquidVMargin() {
        return liquidVMargin;
    }

    public void setLiquidVMargin(int liquidVMargin) {
        this.liquidVMargin = liquidVMargin;
    }
}
