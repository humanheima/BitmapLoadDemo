package com.hm.bitmaploadexample.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.imageloader.ImageLoader;
import com.hm.bitmaploadexample.imageloader.ImageResizer;
import com.hm.bitmaploadexample.utils.Images;
import com.hm.bitmaploadexample.utils.MyUtils;

public class TestActivity extends AppCompatActivity {

    private ImageView imageView;
    private ImageLoader imageLoader;
    private String imgUrl;
    private String tag = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        imageView = (ImageView) findViewById(R.id.img_test);
        imageLoader = ImageLoader.getInstance();
        //imgUrl = getIntent().getStringExtra("imgUrl");
        imgUrl = Images.imageUrls[0];
        if (!TextUtils.isEmpty(imgUrl)) {
            imageLoader.bindBitmap(imgUrl, imageView);
        }
    }
}
