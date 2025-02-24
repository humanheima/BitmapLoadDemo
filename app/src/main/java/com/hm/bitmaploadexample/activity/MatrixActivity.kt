package com.hm.bitmaploadexample.activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hm.bitmaploadexample.R
import com.hm.bitmaploadexample.databinding.ActivityMatrixBinding
import com.hm.bitmaploadexample.utils.BitmapTransformer

/**
 * Created by p_dmweidu on 2025/2/24
 * Desc: 测试 使用 Matrix 对 Bitmap 进行变换
 */
class MatrixActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatrixBinding

    companion object {

        fun launch(context: Context) {
            val starter = Intent(context, MatrixActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatrixBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTransform.setOnClickListener {
            //rotate()
            //scale()
            //translate()
            //skew()
            composeTransform()
        }
    }

    private fun rotate() {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.balloon)
        binding.ivOriginal.setImageBitmap(originalBitmap)
        val transformBitmap = BitmapTransformer.rotateBitmap(originalBitmap, 20f)
        binding.ivTransformed.setImageBitmap(transformBitmap)
    }

    private fun scale() {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.balloon)
        binding.ivOriginal.setImageBitmap(originalBitmap)

        val scaleBitmap = BitmapTransformer.scaleBitmap(originalBitmap, 1.5f, 1.5f)
        binding.ivTransformed.setImageBitmap(scaleBitmap)
    }

    private fun translate() {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.balloon)
        binding.ivOriginal.setImageBitmap(originalBitmap)
        val transformBitmap = BitmapTransformer.translateBitmap(originalBitmap, 100f, 0f)
        binding.ivTransformed.setImageBitmap(transformBitmap)
    }

    private fun skew() {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.balloon)
        binding.ivOriginal.setImageBitmap(originalBitmap)
        //val transformBitmap = BitmapTransformer.skewBitmap(originalBitmap, 0.5f, 0f)
        //val transformBitmap = BitmapTransformer.skewBitmap(originalBitmap, -0.5f, 0f)
        //val transformBitmap = BitmapTransformer.skewBitmap(originalBitmap, 0f, 0.3f)
        val transformBitmap = BitmapTransformer.skewBitmap(originalBitmap, 0f, -0.3f)
        binding.ivTransformed.setImageBitmap(transformBitmap)
    }

    private fun composeTransform() {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.balloon)
        binding.ivOriginal.setImageBitmap(originalBitmap)
        val translateBitmap = BitmapTransformer.composeTransformBitmap(originalBitmap, 1.5f, 60f)
        binding.ivTransformed.setImageBitmap(translateBitmap)
    }


}
