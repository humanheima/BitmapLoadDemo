package com.hm.bitmaploadexample.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import com.hm.bitmaploadexample.databinding.ActivityBitmapHeightProblemTestBinding

/**
 * Created by p_dmweidu on 2024/6/20
 * Desc: 测试 ImageView在键盘弹起时，有时候会重新布局，有时候不会重新布局的问题
 */
class BitmapHeightProblemTestActivity : AppCompatActivity() {


    private lateinit var binding: ActivityBitmapHeightProblemTestBinding

    companion object {

        private const val TAG = "BitmapHeightProblemTest"


        fun launch(context: Context) {
            val starter = Intent(context, BitmapHeightProblemTestActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBitmapHeightProblemTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ImmersionBar
            .with(this)
            .titleBar(binding.ivClosePage)
            .keyboardEnable(true)
            .setOnKeyboardListener { isPopup, _ ->
                val decorView = window.decorView
                if (isPopup) {
                    //软键盘弹出的时候，去掉RecyclerView的图层混合效果
                    Log.i(
                        TAG,
                        "setOnKeyboardListener:软键盘弹起？ isPopup = $isPopup decorView height = ${decorView.height}"
                    )
                } else {
                    Log.i(
                        TAG,
                        "setOnKeyboardListener:软键盘弹起？ isPopup = $isPopup decorView height = ${decorView.height}"
                    )
                }
            }
            .init()

        val url = "https://zmdcharactercdn.zhumengdao.com/0566bcda741e8053f24b3fa3d765beea.png"
        //val url = "https://imgservices-1252317822.image.myqcloud.com/coco/s11152023/bf3b4a97.jzoi4c.jpg"
        //val url = "https://xxvirtualcharactercdn.xxsypro.com/8B0C6E618460014119F1537BBBEFEA18.jpg"
        //YWImageLoader.loadImage(binding.ivBottomBg, url)
        Glide.with(this).load(url).into(binding.ivBigImage)
    }
}