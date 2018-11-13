package com.hm.bitmaploadexample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.hm.bitmaploadexample.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 正确使用Handler的方式
 * 参考链接
 * https://my.oschina.net/rengwuxian/blog/181449
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_grid_view)
    Button btnGridView;
    @BindView(R.id.btn_photo_wall)
    Button btnPhotoWall;
    @BindView(R.id.btn_test)
    Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_grid_view, R.id.btn_photo_wall, R.id.btn_test, R.id.btn_load_large_image})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_grid_view:
                startActivity(new Intent(MainActivity.this, GridViewActivity.class));
                break;
            case R.id.btn_photo_wall:
                startActivity(new Intent(MainActivity.this, GlideActivity.class));
                break;
            case R.id.btn_test:
                TestActivity.launch(this);
                break;
            case R.id.btn_load_large_image:
                LargeImageViewActivity.launch(this);
                break;
            default:
                break;
        }
    }
}
