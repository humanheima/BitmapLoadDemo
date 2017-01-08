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

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_grid_view)
    Button btnGridView;
    @BindView(R.id.btn_photo_wall)
    Button btnPhotoWall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_grid_view, R.id.btn_photo_wall})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_grid_view:
                startActivity(new Intent(MainActivity.this, GridViewActivity.class));
                break;
            case R.id.btn_photo_wall:
                startActivity(new Intent(MainActivity.this, GlideActivity.class));
                break;
        }
    }
}
