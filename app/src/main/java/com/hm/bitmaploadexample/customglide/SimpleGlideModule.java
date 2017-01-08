package com.hm.bitmaploadexample.customglide;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by Administrator on 2017/1/6.
 * 自定义glided的行为,需要在AndroidManifest.xml文件中声明
 * <meta-data
 * android:name="com.hm.bitmaploadexample.customglide.SimpleGlideModule"
 * android:value="GlideModule" />
 */
public class SimpleGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

        //提高图片质量
        //builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        int defaultMemCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();

        int customMemoryCacheSize = (int) (1.2 * defaultMemCacheSize);
        int customBitmapPoolSize = (int) (1.2 * defaultBitmapPoolSize);
        //自定义内存缓存大小
        builder.setMemoryCache(new LruResourceCache(customMemoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));
        //自定义磁盘缓存,100MB
        int diskCacheSize = 100 * 1024 * 1024;
        // builder.setDiskCache(new InternalCacheDiskCacheFactory(context,diskCacheSize));
        //builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, diskCacheSize));
        //自定义缓存路径
        String mCacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();

        Log.e("mCacheDir", mCacheDir);
        builder.setDiskCache(new DiskLruCacheFactory(mCacheDir, "hm_cache", diskCacheSize));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
