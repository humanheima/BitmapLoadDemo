package com.hm.bitmaploadexample.transform;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import androidx.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * Created by dumingwei on 2017/1/8.
 * 重用变换
 * Transformation 的设计初衷是无状态的。
 * 因此，在多个加载中复用 Transformation 应当总是安全的。
 * 创建一次 Transformation 并在多个加载中使用它，通常是很好的实践。
 */
public class BlurTransformation extends BitmapTransformation {

    private static final String TAG = "BlurTransformation";

    private static final String ID = "com.hm.bitmaploadexample.transform.GlideRoundTransform";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private RenderScript rs;
    private int radius = 10;

    public BlurTransformation(Context context, int radius) {
        rs = RenderScript.create(context);
        this.radius = radius;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {

        Log.i(TAG, "transform: \n" + Log.getStackTraceString(new Throwable()));

        Bitmap blurredBitmap = toTransform.copy(Bitmap.Config.ARGB_8888, true);

        // Allocate memory for Renderscript to work with
        Allocation input = Allocation.createFromBitmap(
                rs,
                blurredBitmap,
                Allocation.MipmapControl.MIPMAP_FULL,
                Allocation.USAGE_SHARED
        );
        Allocation output = Allocation.createTyped(rs, input.getType());

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);

        // Set the blur radius
        script.setRadius(radius);

        // Start the ScriptIntrinisicBlur
        script.forEach(output);

        // Copy the output to the blurred bitmap
        output.copyTo(blurredBitmap);

        //toTransform.recycle();

        return blurredBitmap;

    }

    @Override
    public int hashCode() {
        return Util.hashCode(ID.hashCode(), Util.hashCode(radius));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlurTransformation) {
            BlurTransformation other = (BlurTransformation) o;
            return this.radius == other.radius;
        }
        return false;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        byte[] radiusData = ByteBuffer.allocate(4).putInt(radius).array();
        messageDigest.update(radiusData);
    }
}
