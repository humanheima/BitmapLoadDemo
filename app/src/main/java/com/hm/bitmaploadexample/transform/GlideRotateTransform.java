package com.hm.bitmaploadexample.transform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * 将图像做旋转操作
 */
public class GlideRotateTransform extends BitmapTransformation {
    private float rotateAngle = 0f;

    public GlideRotateTransform(Context context) {
        this(context, 90);
    }

    public GlideRotateTransform(Context context, float rotateAngle) {
        super(context);
        this.rotateAngle = rotateAngle;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateAngle);
        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
    }

    @Override
    public String getId() {
        return getClass().getName() + rotateAngle;
    }
}