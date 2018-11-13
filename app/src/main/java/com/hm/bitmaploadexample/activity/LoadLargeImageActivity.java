package com.hm.bitmaploadexample.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.utils.ScreenUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoadLargeImageActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.iv_big_image)
    ImageView ivBigImage;

    public static void launch(Context context) {
        Intent intent = new Intent(context, LoadLargeImageActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_big_image);
        ButterKnife.bind(this);
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.member_benifit)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.e(TAG, "onResourceReady: height=" + resource.getHeight() + "width=" + resource.getWidth());
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ivBigImage.getLayoutParams();
                        params.height = ScreenUtil.width(LoadLargeImageActivity.this).px * resource.getHeight() / resource.getWidth();
                        ivBigImage.setLayoutParams(params);
                        ivBigImage.setImageBitmap(resource);
                    }
                });
    }
}
