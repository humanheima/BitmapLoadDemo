package com.hm.bitmaploaddemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.hm.bitmaploaddemo.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btnGridView = (Button) findViewById(R.id.btn_grid_view);
        btnGridView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_grid_view:
                startActivity(new Intent(MainActivity.this, GridViewActivity.class));
                break;
            default:
                break;
        }
    }
}
