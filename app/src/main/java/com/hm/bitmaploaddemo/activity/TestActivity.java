package com.hm.bitmaploaddemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.hm.bitmaploaddemo.R;
import com.hm.bitmaploaddemo.utils.MyUtils;

import imgloader.ImageLoader;

public class TestActivity extends AppCompatActivity {

    private ImageView imageView;
    private ImageLoader imageLoader;
    private String imgUrl;
    private int screenWidth;
    private int imgHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        imageView = (ImageView) findViewById(R.id.img_test);
        imageLoader = ImageLoader.build();
        screenWidth = MyUtils.getScreenMetrics(this).widthPixels;
        imgHeight = (int) MyUtils.dp2px(this, 160);
        imgUrl = getIntent().getStringExtra("imgUrl");
        if (!TextUtils.isEmpty(imgUrl)) {
            imageLoader.bindBitmap(imgUrl, imageView, screenWidth, imgHeight);
        }
    }
}
