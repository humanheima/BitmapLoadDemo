package com.hm.bitmaploadexample.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.imageloader.ImageLoader;
import com.hm.bitmaploadexample.utils.Images;

import java.lang.ref.WeakReference;

public class TestActivity extends AppCompatActivity {

    public static final int MESSAGE_WHAT = 15;
    private ImageView imageView;
    private ImageLoader imageLoader;
    private String imgUrl;
    private String TAG = getClass().getSimpleName();
    private MyHandler handler;

    public static void launch(Context context) {
        Intent starter = new Intent(context, TestActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        imageView = findViewById(R.id.img_test);
        imageLoader = ImageLoader.getInstance();
        handler = new MyHandler(this);
        imgUrl = Images.imageUrls[0];
        handler.sendEmptyMessage(MESSAGE_WHAT);
    }

    private void setImage() {
        imageLoader.bindBitmap(imgUrl, imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static class MyHandler extends Handler {

        WeakReference<TestActivity> mActivityReference;

        public MyHandler(TestActivity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WHAT:
                    TestActivity activity = mActivityReference.get();
                    if (activity != null) {
                        activity.setImage();
                    }
                    break;
                default:
                    break;
            }

        }
    }
}



