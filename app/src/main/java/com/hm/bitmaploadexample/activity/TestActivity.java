package com.hm.bitmaploadexample.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.imageloader.ImageLoader;
import com.hm.bitmaploadexample.utils.MyUtils;

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
        imageLoader = ImageLoader.getInstance();
        screenWidth = MyUtils.getScreenMetrics(this).widthPixels;
        imgHeight = (int) MyUtils.dp2px(this, 160);
        imgUrl = getIntent().getStringExtra("imgUrl");
        if (!TextUtils.isEmpty(imgUrl)) {
            imageLoader.bindBitmap(imgUrl, imageView, screenWidth, imgHeight);
        }
    }
}
