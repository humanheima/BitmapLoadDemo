package com.hm.bitmaploadexample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.hm.bitmaploadexample.R;


/**
 * 正确使用Handler的方式
 * 参考链接
 * https://my.oschina.net/rengwuxian/blog/181449
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnGridView;
    Button btnPhotoWall;
    Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_test_image_size) {
            ImageSizeForMemoryTestActivity.launch(this);
        } else if (id == R.id.btn_grid_view) {
            startActivity(new Intent(MainActivity.this, GridViewActivity.class));
        } else if (id == R.id.btn_glide_all) {
            startActivity(new Intent(MainActivity.this, GlideActivity.class));
        } else if (id == R.id.btn_test) {
            TestActivity.launch(this);
        } else if (id == R.id.btn_load_large_image) {
            LargeImageViewActivity.launch(this);
        } else if (id == R.id.btn_source_code) {
            startActivity(new Intent(MainActivity.this, GlideSourceCodeActivity.class));
        }
    }
}
