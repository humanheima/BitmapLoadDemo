package com.hm.bitmaploadexample.customglide;

import android.content.Context;

import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

/**
 * Created by Administrator on 2017/1/8.
 */
public class CustomImageSizeUrlLoader extends BaseGlideUrlLoader<CustomImageSizeModel> {

    public CustomImageSizeUrlLoader(Context context) {
        super(context);
    }

    @Override
    protected String getUrl(CustomImageSizeModel model, int width, int height) {
        return model.requestCustomSizeUrl(width, height);
    }
}
