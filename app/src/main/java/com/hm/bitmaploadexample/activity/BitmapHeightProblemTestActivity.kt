package com.hm.bitmaploadexample.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hm.bitmaploadexample.databinding.ActivityBitmapHeightProblemTest2Binding


/**
 * Created by p_dmweidu on 2024/6/20
 * Desc: 测试 ImageView在键盘弹起时，有时候会重新布局，有时候不会重新布局的问题
 */
class BitmapHeightProblemTestActivity : AppCompatActivity() {


    //private lateinit var binding: ActivityBitmapHeightProblemTestBinding
    private lateinit var binding: ActivityBitmapHeightProblemTest2Binding

    companion object {

        private const val TAG = "BitmapHeightProblemTest"

        fun launch(context: Context) {
            val starter = Intent(context, BitmapHeightProblemTestActivity::class.java)
            context.startActivity(starter)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //和在xml中设置  android:windowSoftInputMode="adjustResize"
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        //binding = ActivityBitmapHeightProblemTestBinding.inflate(layoutInflater)
        binding = ActivityBitmapHeightProblemTest2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val decorView = window.decorView
        val rootView = binding.rootView
        val ivBigImage = binding.ivBigImage

        rootView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            Log.i(
                TAG,
                "setOnScrollChangeListener: decorView height = ${decorView.height} top = ${decorView.top} bottom = ${decorView.bottom} scrolY = $scrollY oldScrollY = $oldScrollY"
            )
        }

        binding.btnHeight.setOnClickListener {
            getScrollY(binding.rootView)
            Log.i(
                TAG,
                "setOnKeyboardListener: decorView height = ${decorView.height} top = ${decorView.top} bottom = ${decorView.bottom} scrollY = ${decorView.scrollY}"
            )

            Log.i(
                TAG,
                "setOnKeyboardListener: 根布局 height = ${rootView.height}  top = ${rootView.top} bottom = ${rootView.bottom} scrollY =  ${rootView.scrollY}"
            )

            Log.i(
                TAG,
                "setOnKeyboardListener: 大ImageView height = ${ivBigImage.height}  top = ${ivBigImage.top} bottom = ${ivBigImage.bottom} scrollY =  ${ivBigImage.scrollY} "
            )


        }

        val url = "https://xxvirtualcharactercdn.xxsypro.com/8B0C6E618460014119F1537BBBEFEA18.jpg"
        //YWImageLoader.loadImage(binding.ivBottomBg, url)
        Glide.with(this).load(url).into(binding.ivBigImage)
    }

    private fun getScrollY(view: View) {
        var scrollY = view.scrollY
        Log.i(TAG, "getScrollY: view = $view scrollY = $scrollY")
        var parent = view.parent
        if (parent is View) {
            getScrollY(parent)
        }
    }
}