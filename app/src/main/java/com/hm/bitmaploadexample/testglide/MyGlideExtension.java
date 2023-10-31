package com.hm.bitmaploadexample.testglide;

import static com.bumptech.glide.request.RequestOptions.decodeTypeOf;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.annotation.GlideType;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;

@GlideExtension
public class MyGlideExtension {

    public static final int MINI_SIZE = 50;

    private static final RequestOptions DECODE_TYPE_GIF = decodeTypeOf(GifDrawable.class).lock();


    private MyGlideExtension() {
    }

    @GlideOption
    public static BaseRequestOptions<?> miniThumb(BaseRequestOptions<?> options) {
        return options
                .override(MINI_SIZE, MINI_SIZE)
                .fitCenter()
                .skipMemoryCache(true);
    }

    @GlideOption
    public static BaseRequestOptions<?> miniThumb(BaseRequestOptions<?> options, int size) {
        return options
                .override(size, size)
                .skipMemoryCache(true);
    }

    @GlideType(GifDrawable.class)
    public static RequestBuilder<GifDrawable> mAsGif(RequestBuilder<GifDrawable> requestBuilder) {
        return requestBuilder
                .transition(new DrawableTransitionOptions())
                .apply(DECODE_TYPE_GIF);
    }

}
