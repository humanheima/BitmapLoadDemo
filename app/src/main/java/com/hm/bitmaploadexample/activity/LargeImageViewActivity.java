package com.hm.bitmaploadexample.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hm.bitmaploadexample.R;

import java.io.File;

public class LargeImageViewActivity extends AppCompatActivity {

    SubsamplingScaleImageView scaleImageView;

    public static void launch(Context context) {
        Intent intent = new Intent(context, LargeImageViewActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_large_image_view);

        scaleImageView = findViewById(R.id.iv_large_image_view);

        //loadFromLocal();
        loadFromNetworkWithGlide();
    }

    private void loadFromLocal() {
        scaleImageView.setMinimumDpi(160);
        scaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
        scaleImageView.setImage(ImageSource.resource(R.drawable.member_benifit));
    }

    private void loadFromNetworkWithGlide() {
        Glide.with(this).downloadOnly()
                .load("https://image.youshikoudai.com/appConfigs/20181113android.jpg")
                .into(new CustomViewTarget<SubsamplingScaleImageView, File>(scaleImageView) {
                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {

                    }

                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        scaleImageView.setMinimumDpi(160);
                        scaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                        scaleImageView.setImage(ImageSource.uri(Uri.fromFile(resource)));
                    }
                });
               /* .into(new FileTarget() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        scaleImageView.setMinimumDpi(160);
                        scaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                        scaleImageView.setImage(ImageSource.uri(Uri.fromFile(resource)));
                    }
                });*/
    }

}
