package com.hm.bitmaploadexample.activity.sourcecode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hm.bitmaploadexample.R;
import com.hm.bitmaploadexample.adapter.RvAdapter;
import com.hm.bitmaploadexample.utils.Images;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by p_dmweidu on 2023/11/4
 * Desc: 测试Glide 在RecyclerView中的使用
 */
public class GlideRecyclerViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RvAdapter adapter;
    private List<String> mUrList = new ArrayList<>();


    public static void launch(Context context) {
        Intent starter = new Intent(context, GlideRecyclerViewActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide_rv);
        recyclerView = findViewById(R.id.rv_glide);
        initData();
        setAdapter();
    }

    private void initData() {
        Collections.addAll(mUrList, Images.imageUrls);
    }

    private void setAdapter() {
        adapter = new RvAdapter(mUrList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String url = mUrList.get(position);
//                Intent intent = new Intent(GlideRecyclerViewActivity.this, TestActivity.class);
//                intent.putExtra("imgUrl", url);
//                startActivity(intent);
//            }
//        });
    }

}
