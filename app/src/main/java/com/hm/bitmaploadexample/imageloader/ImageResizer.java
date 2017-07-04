package com.hm.bitmaploadexample.imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hm.bitmaploadexample.App;

import java.io.FileDescriptor;
import java.lang.reflect.Field;

/**
 * Created by dumingwei on 2017/1/5.
 */
public class ImageResizer {

    public ImageResizer() {
    }

    private static DisplayMetrics displayMetrics = App.getInstance().getResources().getDisplayMetrics();

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    /**
     * 返回图片的尺寸
     *
     * @param imageView
     * @return
     */
    public static RequestImageSize getImageViewSize(ImageView imageView) {

        RequestImageSize requestImageSize = new RequestImageSize();
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        int width = imageView.getWidth();// 获取imageview的实际宽度
        if (width <= 0) {
            width = lp.width;// 获取imageview在layout中声明的宽度
        }
        if (width <= 0) {
            width = getImageViewFieldValue(imageView, "mMaxWidth");
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;
        }

        int height = imageView.getHeight();// 获取imageview的实际高度
        if (height <= 0) {
            height = lp.height;// 获取imageview在layout中声明的宽度
        }
        if (height <= 0) {
            height = getImageViewFieldValue(imageView, "mMaxHeight");// 检查最大值
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }
        requestImageSize.width = width;
        requestImageSize.height = height;
        return requestImageSize;
    }

    /**
     * 使用反射获取 imageView 的最大宽度或者高度
     *
     * @param imageView
     * @param mMaxField
     * @return imageView 的最大宽度或者高度
     */
    private static int getImageViewFieldValue(ImageView imageView, String mMaxField) {
        int requestField = 0;
        try {
            Field field = ImageView.class.getDeclaredField(mMaxField);
            field.setAccessible(true);//设置是否允许访问，因为该变量是private的，所以要手动设置允许访问，如果msg是public的就不需要这行了。
            requestField = field.getInt(imageView);
        } catch (NoSuchFieldException e) {
            Log.e("getImageViewFieldValue", "NoSuchFieldException:" + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e("getImageViewFieldValue", "IllegalAccessException:" + e.getMessage());
        }
        Log.e("getImageViewFieldValue", "requestField=" + requestField);
        return requestField;
    }

    public static class RequestImageSize {
        public int width;
        public int height;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}
