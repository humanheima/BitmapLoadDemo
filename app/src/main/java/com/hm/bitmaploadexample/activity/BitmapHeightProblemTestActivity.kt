package com.hm.bitmaploadexample.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hm.bitmaploadexample.databinding.ActivityBitmapHeightProblemTest2Binding
import com.hm.bitmaploadexample.databinding.ActivityBitmapHeightProblemTestBinding


/**
 * Created by p_dmweidu on 2024/6/20
 * Desc: 测试 ImageView在键盘弹起时，有时候会重新布局，有时候不会重新布局的问题
 */
class BitmapHeightProblemTestActivity : AppCompatActivity() {


    private lateinit var binding: ActivityBitmapHeightProblemTestBinding
    //private lateinit var binding: ActivityBitmapHeightProblemTest2Binding

    companion object {

        private const val TAG = "BitmapHeightProblemTest"

        fun launch(context: Context) {
            val starter = Intent(context, BitmapHeightProblemTestActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //和在xml中设置  android:windowSoftInputMode="adjustResize"
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding = ActivityBitmapHeightProblemTestBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnHeight.setOnClickListener {
            val decorView = window.decorView
            Log.i(
                TAG,
                "setOnKeyboardListener: decorView height = ${decorView.height} top = ${decorView.top} bottom = ${decorView.bottom}"
            )

            Log.i(
                TAG,
                "setOnKeyboardListener: 根布局约束布局 height = ${binding.rootView.height}  top = ${binding.rootView.top} bottom = ${binding.rootView.bottom}"
            )

            Log.i(
                TAG,
                "setOnKeyboardListener: 大ImageView height = ${binding.ivBigImage.height}  top = ${binding.ivBigImage.top} bottom = ${binding.ivBigImage.bottom}"
            )

        }

        val url = "https://zmdcharactercdn.zhumengdao.com/0566bcda741e8053f24b3fa3d765beea.png"
        //val url = "https://imgservices-1252317822.image.myqcloud.com/coco/s11152023/bf3b4a97.jzoi4c.jpg"
        //val url = "https://xxvirtualcharactercdn.xxsypro.com/8B0C6E618460014119F1537BBBEFEA18.jpg"
        //YWImageLoader.loadImage(binding.ivBottomBg, url)
        Glide.with(this).load(url).into(binding.ivBigImage)
    }
}