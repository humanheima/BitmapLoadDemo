package com.hm.bitmaploadexample.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import com.hm.bitmaploadexample.R

/**
 * Created by dumingwei on 2021/9/9
 *
 *
 * Desc: 测试res目中尺寸不一样的图片占用尺寸比较
 * 结论：在同一res目中，ImageView尺寸一样，设置不同尺寸的图片做背景，尺寸越大，占用的内存越大。
 */
class ImageSizeForMemoryTestActivity : AppCompatActivity() {


    private val TAG: String = "ImageSizeForMemoryTestA"

    private lateinit var ivBig: ImageView
    private lateinit var ivSmall: ImageView

    companion object {
        @JvmStatic
        fun launch(context: Context) {
            val starter = Intent(context, ImageSizeForMemoryTestActivity::class.java)
            context.startActivity(starter)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_size_for_memory_test)

        ivBig = findViewById(R.id.iv_big)
        ivSmall = findViewById(R.id.iv_small)

        ivBig.postDelayed({
            val bigDrawable = ivBig.background
            if (bigDrawable is BitmapDrawable) {
                Log.i(TAG, "onCreate: bigDrawable.intrinsicWidth = ${bigDrawable.intrinsicWidth} , " +
                        "bigDrawable.intrinsicHeight = ${bigDrawable.intrinsicHeight} ," +
                        " bigDrawable.bitmap.byteCount = ${bigDrawable.bitmap.byteCount}")
            }

            val smallDrawable = ivSmall.background
            if (smallDrawable is BitmapDrawable) {
                Log.i(TAG, "onCreate: smallDrawable.intrinsicWidth = ${smallDrawable.intrinsicWidth} , " +
                        "smallDrawable.intrinsicHeight = ${smallDrawable.intrinsicHeight} ," +
                        " smallDrawable.bitmap.byteCount = ${smallDrawable.bitmap.byteCount}")
            }

        }, 5000)

    }


}