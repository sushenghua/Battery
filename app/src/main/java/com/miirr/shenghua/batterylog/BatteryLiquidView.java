package com.miirr.shenghua.batterylog;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;

import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by shenghua on 12/13/15.
 */
public class BatteryLiquidView extends View {

    private int[] mGradientColors = new int[] {
            getResources().getColor(R.color.batteryLiquidGradientDarker),
            getResources().getColor(R.color.batteryLiguidGradientLighter),
            getResources().getColor(R.color.batteryLiquidGradientDarker)
    };

    private float power = 88;

    // drawable parameters and default values
    private int liquidHMargin = 20;
    private int mLiquidVMargin = 4;


    private int liquidWidth;
    private int liquidMaxHeight;
    private int liquidBottomOffset;

    private Rect mLiquidBounds = new Rect();

    // drawables
    private BitmapDrawable liquidTopEdge;

    // rendering
    private Shader mHorizontalGrayShader;
    private Shader mVerticalColorShader;
    //private Shader mBatteryLiquidShader;
    private Paint paint;

    private WeakReference<BatteryView> containerView;

    public BatteryLiquidView(Context context, BatteryView containerView) {
        super(context);
        this.containerView = new WeakReference<>(containerView);
        init();
    }

//    public BatteryLiquidView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init(attrs, 0);
//    }
//
//    public BatteryLiquidView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        init(attrs, defStyle);
//    }

    private void init() {
        // get parent view
        if (containerView == null)
            return;
        BatteryView parentView = containerView.get();
        if (parentView == null)
            return;

        // calculate params
        liquidWidth = parentView.getLiquidWidth();
        liquidMaxHeight = parentView.getLiquidCoverHeight() - 2 * parentView.getLiquidVMargin();
        liquidBottomOffset = parentView.getBatteryHeight() / 2 - parentView.getPoleHeight()
                - parentView.getLiquidCoverVMargin() - parentView.getLiquidVMargin();

        // bitmap
        Bitmap liquidTopBitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.battery_liquid_top);
        ImageSize s = ImageSize.getBitmapSize(getResources(), R.drawable.battery_liquid_top);
        liquidTopEdge = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(liquidTopBitmap, parentView.getLiquidWidth(), s.height, true));

        // paint
        paint = new Paint();
        float hue;
        if (power > 80)
            hue = 120;
        else if (power > 20)
            hue = (power - 20) / 60 * 120;
        else
            hue = 0;
        paint.setColorFilter(createColorFilter(hue));

        // http://developer.android.com/guide/topics/graphics/hardware-accel.html
        // Same type shaders inside ComposeShader not supported by Hardware-acceleration
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }

        // apply scale
        float scale = parentView.getScale();
        setScaleX(scale);
        setScaleY(scale);
    }

//    private void init(AttributeSet attrs, int defStyle) {
//        // drawable parameters
//        final TypedArray a = getContext().obtainStyledAttributes(
//                attrs, R.styleable.BatteryView, defStyle, 0);
//        try {
//            liquidHMargin = a.getInt(
//                    R.styleable.BatteryView_liquidHMargin, liquidHMargin);
//            mLiquidVMargin = a.getInt(
//                    R.styleable.BatteryView_liquidVMargin, mLiquidVMargin);
//
//            int poleWidth = a.getInt(R.styleable.BatteryView_poleWidth,
//                    BatteryFrontView.POLE_DEFAULT_WIDTH);
//
//            int liquidCoverHeight = a.getInt(R.styleable.BatteryView_liquidCoverHeight,
//                    BatteryFrontView.LIQUID_COVER_DEFAULT_HEIGHT);
//            int liquidCoverHMargin = a.getInt(R.styleable.BatteryView_liquidCoverHMargin,
//                    BatteryFrontView.LIQUID_COVER_DEFAULT_H_MARGIN);
//            int liquidCoverVMargin = a.getInt(R.styleable.BatteryView_liquidCoverVMargin,
//                    BatteryFrontView.LIQUID_COVER_DEFAULT_V_MARGIN);
//
//            liquidWidth = poleWidth - 2 * liquidCoverHMargin - 2 * liquidHMargin;
//            mLiquidMaxHeight = liquidCoverHeight - 2 * mLiquidVMargin;
//            mLiquidBottomOffset =
//                    (BatteryFrontView.POLE_TOP_HEIGHT - BatteryFrontView.POLE_TOP_OVERLAY + liquidCoverHeight +
//                    2 * liquidCoverVMargin + 2 * 64) / 2 - 64 - liquidCoverVMargin - mLiquidVMargin - 4;
//        } finally {
//            a.recycle();
//        }
//
//        // bitmap
//        Bitmap liquidTopBitmap = BitmapFactory.decodeResource(
//                getResources(), R.drawable.battery_liquid_top);
//        ImageSize s = ImageSize.getBitmapSize(getResources(),
//                R.drawable.battery_liquid_top);
//        liquidTopEdge = new BitmapDrawable(getResources(),
//                Bitmap.createScaledBitmap(liquidTopBitmap, liquidWidth, s.height, true));
//
//
//        power = 50;
//        // paint
//        paint = new Paint();
//        float hue;
//        if (power > 80)
//            hue = 120;
//        else if (power > 20)
//            hue = (power - 20)/60 * 120;
//        else
//            hue = 0;
//        paint.setColorFilter(createColorFilter(hue));
//
//        // http://developer.android.com/guide/topics/graphics/hardware-accel.html
//        // Same type shaders inside ComposeShader not supported by Hardware-acceleration
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
////            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
////        }
//
//        // apply scale
//        setScaleX(scale);
//        setScaleY(scale);
//    }

    private ColorMatrixColorFilter createColorFilter(float hueValue) {
        int color = Color.HSVToColor(new float[]{hueValue, 1.0f, 1.0f});
        //Log.d("---------> color", Integer.toHexString(color));
        float r = Color.red(color)/255.0f;
        float g = Color.green(color)/255.0f;
        float b = Color.blue(color)/255.0f;
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
                r, 0, 0, 0, 0,
                0, g, 0, 0, 0,
                0, 0, b, 0, 0,
                0, 0, 0, 1, 0
        });
        return new ColorMatrixColorFilter(colorMatrix);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // decide center
        boolean inPortraitOrientation = w < h;
        int cX = w / 2;
        int cY = h / 2;

        // liquid top
        int r = liquidWidth / 2;
        int t = cY + liquidBottomOffset - (int)(liquidMaxHeight * power /100.0);
        int b = t + liquidTopEdge.getIntrinsicHeight();
        liquidTopEdge.setBounds(cX - r, t, cX + r, b);

        // horizontal light/shadow gradient
        mHorizontalGrayShader = new LinearGradient(cX - r , t, cX + r, b,
                mGradientColors, null, Shader.TileMode.CLAMP);
        paint.setShader(mHorizontalGrayShader);

//        // vertical color gradient
//        int[] redGreenColors = new int[] {
//                getResources().getColor(R.color.batteryLiquidGreen),
//                getResources().getColor(R.color.batteryLiquidRed)
//        };
//        mVerticalColorShader = new LinearGradient(0, 0, 0, 1, redGreenColors,
//                null, Shader.TileMode.CLAMP);
//        Matrix matrix = new Matrix();
//        matrix.setRotate(90);
//        mVerticalColorShader.setLocalMatrix(matrix);
//
//        // composed liquid effect
//        mBatteryLiquidShader = new ComposeShader(mHorizontalGrayShader, mVerticalColorShader,
//                new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        // liquid bounds
        mLiquidBounds.left = cX - r;
        mLiquidBounds.top = t + liquidTopEdge.getIntrinsicHeight() / 2 - 2;
        mLiquidBounds.right = cX + r;
        mLiquidBounds.bottom = cY + liquidBottomOffset;

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(mLiquidBounds.left, mLiquidBounds.top,
                mLiquidBounds.right, mLiquidBounds.bottom, paint);

        liquidTopEdge.draw(canvas);
    }
}
