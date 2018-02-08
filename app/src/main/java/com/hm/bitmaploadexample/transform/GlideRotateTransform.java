package com.hm.bitmaploadexample.transform;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * 将图像做旋转操作
 */
public class GlideRotateTransform extends BitmapTransformation {

    private static final String ID = "com.hm.bitmaploadexample.transform.GlideRoundTransform";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private int rotateAngle = 0;

    public GlideRotateTransform(int rotateAngle) {
        this.rotateAngle = rotateAngle;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateAngle);
        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        byte[] radiusData = ByteBuffer.allocate(4).putInt(rotateAngle).array();
        messageDigest.update(radiusData);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GlideRotateTransform) {
            GlideRotateTransform other = (GlideRotateTransform) o;
            return rotateAngle == other.rotateAngle;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Util.hashCode(ID.hashCode(), Util.hashCode(rotateAngle));
    }

}