package com.shenghua.battery;

import android.content.res.Resources;
import android.graphics.BitmapFactory;

/**
 * Created by shenghua on 12/13/15.
 */
public class ImageSize {

    public int width;
    public int height;

    static ImageSize getBitmapSize(Resources res, int resId) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, option);
        return new ImageSize(option.outWidth, option.outHeight);
    }

    static ImageSize getBitmapSize(byte[] bytes) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, option);
        return new ImageSize(option.outWidth, option.outHeight);
    }

    ImageSize(int w, int h) {
        width = w;
        height = h;
    }

    public String toString() {
        return "(width: " + width + ", height: " + height + ")";
    }
}
