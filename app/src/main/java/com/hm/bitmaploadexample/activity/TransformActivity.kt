package com.hm.bitmaploadexample.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hm.bitmaploadexample.databinding.ActivityTransformBinding
import com.hm.bitmaploadexample.transform.TopCrop
import com.hm.bitmaploadexample.utils.Images

/**
 * Created by p_dmweidu on 2025/2/28
 * Desc: 测试 TopCrop 变换
 */
class TransformActivity : AppCompatActivity() {


    private lateinit var binding: ActivityTransformBinding

    companion object {

        fun launch(context: Context) {
            val starter = Intent(context, TransformActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransformBinding.inflate(layoutInflater)
        setContentView(binding.root)
        glideTopCrop()

    }

    /**
     *
     * 测试 TopCrop 变换 ，nice
     */
    private fun glideTopCrop() {
        Glide.with(this).load(Images.imageUrls[5])
            //Glide.with(this).load(R.drawable.balloon_31_32)
            //Glide.with(this).load(R.drawable.balloon_31_32)
            .apply(
                RequestOptions().transform(
                    TopCrop()
                )
            )
            .into(binding.ivFixXy)

        Glide.with(this).load(Images.imageUrls[5])
            //Glide.with(this).load(R.drawable.balloon4_2)
            //Glide.with(this).load(R.drawable.balloon_31_32)
            .apply(
                RequestOptions().transform(
                    TopCrop()
                )
            )
            .into(binding.ivFixXySmall)

        Glide.with(this).load(Images.imageUrls[5])
            //Glide.with(this).load(R.drawable.balloon_31_32)
            .apply(
                RequestOptions().transform(
                    TopCrop()
                )
            )
            .into(binding.ivFixXy23)

        Glide.with(this).load(Images.imageUrls[5])
            //Glide.with(this).load(R.drawable.balloon4_2)
            //Glide.with(this).load(R.drawable.balloon_31_32)
            .apply(
                RequestOptions().transform(
                    TopCrop()
                )
            )
            .into(binding.ivFixXySmall23)
    }
}