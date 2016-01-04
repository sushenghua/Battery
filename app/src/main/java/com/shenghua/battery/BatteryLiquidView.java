package com.shenghua.battery;

import android.content.Context;
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
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by shenghua on 12/13/15.
 */
public class BatteryLiquidView extends View {

    // liquid lighting shadowing gradient colors
    private int[] mGradientColors = new int[] {
            getResources().getColor(R.color.batteryLiquidGradientDarker),
            getResources().getColor(R.color.batteryLiguidGradientLighter),
            getResources().getColor(R.color.batteryLiquidGradientDarker)
    };

    // power determines the height of liquid
    private float power = 0;

    // appearance args
    private int centerX;
    private int centerY;
    private int liquidWidth = 464;
    private int liquidMaxHeight;
    private int liquidBottomOffset;
    private Rect liquidBounds = new Rect();

    // drawables
    private BitmapDrawable liquidTopEdge;

    // rendering
    private Shader mHorizontalGrayShader;
    private Shader mVerticalColorShader;
    //private Shader mBatteryLiquidShader;
    private Paint paint;

    // weak ref to parent view
    private WeakReference<BatteryView> parentView;

    public BatteryLiquidView(Context context, BatteryView parentView) {
        super(context);
        this.parentView = new WeakReference<>(parentView);
        init();
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

        power = parentView.getPower();

        // calculate params
        liquidWidth = parentView.getLiquidWidth();
        liquidMaxHeight = parentView.getLiquidCoverHeight() - 2 * parentView.getLiquidVMargin();
        liquidBottomOffset = parentView.getBatteryHeight() / 2 - parentView.getPoleHeight()
                - parentView.getLiquidCoverVMargin() - parentView.getLiquidVMargin();

        return true;
    }

    private void init() {
        // load appearance params
        if (!updateCachedParams())
            return;

        // bitmap
        //Bitmap liquidTopBitmap = BitmapFactory.decodeResource(
        //        getResources(), R.drawable.battery_liquid_top);
        Bitmap liquidTopBitmap = EmbeddedImage.createBatteryLiquidTopImage();
        ImageSize s = ImageSize.getBitmapSize(getResources(), R.drawable.battery_liquid_top);
        liquidTopEdge = new BitmapDrawable(getResources(),
                Bitmap.createScaledBitmap(liquidTopBitmap, liquidWidth, s.height, true));

        // paint
        paint = new Paint();
        setPower(power);

        // http://developer.android.com/guide/topics/graphics/hardware-accel.html
        // Same type shaders inside ComposeShader not supported by Hardware-acceleration
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        //    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //}
    }

    public void setPower(float p) {
        power = p;
        float hue;
        if (power > 80)
            hue = 120;
        else if (power > 20)
            hue = (power - 20) / 60 * 120;
        else
            hue = 0;
        paint.setColorFilter(createColorFilter(hue));
        updateBounds();
        invalidate();
    }

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

    private void updateBounds() {
        // liquid top
        int r = liquidWidth / 2;
        int t = centerY + liquidBottomOffset - (int)(liquidMaxHeight * power /100.0);
        int b = t + liquidTopEdge.getIntrinsicHeight();
        liquidTopEdge.setBounds(centerX - r, t, centerX + r, b);

        // horizontal light/shadow gradient
        mHorizontalGrayShader = new LinearGradient(centerX - r , t, centerX + r, b,
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
        liquidBounds.left = centerX - r;
        liquidBounds.top = t + liquidTopEdge.getIntrinsicHeight() / 2 - 2;
        liquidBounds.right = centerX + r;
        liquidBounds.bottom = centerY + liquidBottomOffset;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (updateCachedParams())
            updateBounds();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(liquidBounds.left, liquidBounds.top,
                liquidBounds.right, liquidBounds.bottom, paint);

        liquidTopEdge.draw(canvas);
    }
}
