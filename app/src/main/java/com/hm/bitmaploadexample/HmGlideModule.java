package com.hm.bitmaploadexample;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by dumingwei on 2018/2/8 0008.
 */
@GlideModule()
public class HmGlideModule extends AppGlideModule {

    private final String TAG = getClass().getName();

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(2)
                .setBitmapPoolScreens(3)
                .build();
        int memoryCacheSize = calculator.getMemoryCacheSize();
        Log.e(TAG, "applyOptions: memoryCacheSize=" + memoryCacheSize / 1024 / 1024);
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
        int bitmapPoolSize = calculator.getBitmapPoolSize();
        Log.e(TAG, "applyOptions: bitmapPoolSize=" + bitmapPoolSize / 1024 / 1024);
        builder.setBitmapPool(new LruBitmapPool(bitmapPoolSize));
        int diskCacheSizeBytes = 1024 * 1024 * 100; // 100 MB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }
}
