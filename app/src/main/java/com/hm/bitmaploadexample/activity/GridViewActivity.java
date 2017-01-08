package com.hm.bitmaploadexample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.adapter.ImageAdapter;
import com.hm.bitmaploadexample.utils.CacheUtil;
import com.hm.bitmaploadexample.utils.Images;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class GridViewActivity extends AppCompatActivity {

    private GridView gridView;
    private ImageAdapter adapter;
    private List<String> mUrList = new ArrayList<>();
    private Timer timer;
    private MyTimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        gridView = (GridView) findViewById(R.id.grid_view);
        timer = new Timer();
        timerTask = new MyTimerTask();
        timer.schedule(timerTask, 2000, 5000);
        initData();
        setAdapter();
       /* Picasso.with(this).load("").into(new ImageView(this));*/
    }

    private void initData() {
        for (String url : Images.imageUrls) {
            mUrList.add(url);
        }
    }

    private void setAdapter() {
        adapter = new ImageAdapter(gridView, mUrList, this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = mUrList.get(position);
                Intent intent = new Intent(GridViewActivity.this, TestActivity.class);
                intent.putExtra("imgUrl", url);
                startActivity(intent);
            }
        });
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("mainActivity", "cache size:" + CacheUtil.getAutoFileOrFilesSize(getDiskCacheDir()));
                }
            });
        }
    }

    private String getDiskCacheDir() {
        return getExternalCacheDir().getPath() + File.separator + "hm_bitmap/";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
