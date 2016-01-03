package com.shenghua.battery;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by shenghua on 1/2/16.
 */
class IndicatorView extends ImageView {

    public static final int INDICATOR_DEAD = 0;
    public static final int INDICATOR_UNSUPPORT = 1;
    public static final int INDICATOR_SUPPORT = 2;
    public static final int INDICATOR_GOOD = 3;
    public static final int INDICATOR_ABNORMAL = 4;
    public static final int INDICATOR_WARNING = 5;
    public static final int INDICATOR_INACTIVE = 6;

    private static final int[] INDICATOR_STATE = {
            R.attr.indicator_value
//            R.attr.indicator_support,
//            R.attr.indicator_good,
//            R.attr.indicator_notgood,
//            R.attr.indicator_warning,
//            R.attr.indicator_inactive,
//            R.attr.indicator_unsupport
    };

    private int indicatorValue = 0;

    public IndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    @Override
//    public int[] onCreateDrawableState(int extraSpace) {
//        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
//        if (indicatorValue >= INDICATOR_UNSUPPORT && indicatorValue <= INDICATOR_INACTIVE) {
//            mergeDrawableStates(drawableState, INDICATOR_STATE);
//        }
//        return drawableState;
//
////        return INDICATOR_STATE;
//    }

    public void setIndicatorValue(int value) {

        if (value != indicatorValue) {
            indicatorValue = value;
            switch (indicatorValue) {
                case INDICATOR_DEAD:
                case INDICATOR_UNSUPPORT:
                    setImageLevel(0);
                    break;

                case INDICATOR_SUPPORT:
                case INDICATOR_GOOD:
                    setImageLevel(1);
                    break;

                case INDICATOR_ABNORMAL:
                    setImageLevel(2);
                    break;

                case INDICATOR_WARNING:
                    setImageLevel(3);
                    break;

                case INDICATOR_INACTIVE:
                    setImageLevel(4);
                    break;
            }
        }
    }
}

