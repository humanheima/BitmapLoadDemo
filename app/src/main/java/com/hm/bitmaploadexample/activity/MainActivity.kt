package com.hm.bitmaploadexample.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hm.bitmaploadexample.R
import com.hm.bitmaploadexample.activity.ImageSizeForMemoryTestActivity.Companion.launch
import com.hm.bitmaploadexample.activity.sourcecode.GlideRecyclerViewActivity
import com.hm.bitmaploadexample.activity.sourcecode.GlideSourceCodeActivity
import com.hm.bitmaploadexample.databinding.ActivityMainBinding

/**
 * 正确使用Handler的方式
 * 参考链接
 * https://my.oschina.net/rengwuxian/blog/181449
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onClick(view: View) {
        val id = view.id
        when (id) {
            R.id.btn_iv_height_problem -> {
                BitmapHeightProblemTestActivity.launch(this)
                //SoftKeyboardActivity.launch(this)
            }

            R.id.btn_test_image_size -> {
                launch(this)
            }

            R.id.btn_grid_view -> {
                startActivity(Intent(this@MainActivity, GridViewActivity::class.java))
            }

            R.id.btn_glide_all -> {
                startActivity(Intent(this@MainActivity, GlideActivity::class.java))
            }

            R.id.btn_test -> {
                TestActivity.launch(this)
            }

            R.id.btn_load_large_image -> {
                LargeImageViewActivity.launch(this)
            }

            R.id.btn_source_code -> {
                startActivity(Intent(this@MainActivity, GlideSourceCodeActivity::class.java))
            }

            R.id.btn_source_code_in_rv -> {
                GlideRecyclerViewActivity.launch(this)
            }
        }
    }
}