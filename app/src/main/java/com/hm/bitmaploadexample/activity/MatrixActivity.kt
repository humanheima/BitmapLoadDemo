package com.hm.bitmaploadexample.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.hm.bitmaploadexample.R
import com.hm.bitmaploadexample.databinding.ActivityMatrixBinding
import com.hm.bitmaploadexample.utils.BitmapTransformer

/**
 * Created by p_dmweidu on 2025/2/24
 * Desc: 测试 使用 Matrix 对 Bitmap 进行变换
 */
class MatrixActivity : AppCompatActivity() {


    private val TAG = "MatrixActivity"

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
            //composeTransform()
            testPreAndPostBitmap()
        }

        testPost()
        testPre()
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

    /**
     * Matrix 的 postXXX 方法测试
     * 计算过程：
     *
     * 1. 缩放：(100, 0) × 2 = (200, 0)。
     * 2. 平移：(200, 0) + (50, 0) = (250, 0)。
     * 3. 结果：(250, 0)。
     */
    private fun testPost() {
        val matrix = Matrix()
        matrix.postScale(2f, 2f)       // 先缩放
        matrix.postTranslate(50f, 0f)  // 再平移

        val points = floatArrayOf(100f, 0f) // 原始点 (100, 0)
        matrix.mapPoints(points)            // 变换后的点
        Log.i(TAG, "testPost: Post result: (${points[0]}, ${points[1]})")
    }

    /**
     * 计算过程：
     *
     * 1. 平移：(100, 0) + (50, 0) = (150, 0)。
     * 2. 缩放：(150, 0) × 2 = (300, 0)。
     * 3. 结果：(300, 0)。
     */
    private fun testPre() {
        val matrix = Matrix()
        matrix.preScale(2f, 2f)        // 后缩放
        matrix.preTranslate(50f, 0f)   // 先平移

        val points = floatArrayOf(100f, 0f) // 原始点 (100, 0)
        matrix.mapPoints(points)            // 变换后的点
        Log.i(TAG, "testPre: Pre result: (${points[0]}, ${points[1]})")

    }


    /**
     * 测试 pre 和 post 的区别
     */
    private fun testPreAndPostBitmap() {

        //val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.balloon)


        // 创建一个简单的原始 Bitmap（100x100，红色矩形）
        val originalBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(originalBitmap)
        val paint = Paint().apply { color = Color.RED }
        canvas.drawRect(0f, 0f, 100f, 100f, paint)

        // 平移和缩放参数
        val dx = 50f
        val dy = 50f
        val scale = 2f

        // 创建更大的 Bitmap，包含平移和缩放后的范围
        //val newWidth = (originalBitmap.width * scale + dx).toInt()
        //val newHeight = (originalBitmap.height * scale + dy).toInt()

        val newWidth = 300
        val newHeight = 300
        //newWidth = 250 , newHeight = 250
        Log.e(TAG, "testPreAndPostBitmap:  newWidth = $newWidth , newHeight = $newHeight")
        val postBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val preBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        // Post: 先缩放后平移
        val postCanvas = Canvas(postBitmap)
        val postMatrix = Matrix().apply {
            //originalBitmap 宽高是100x100 绘制 originalBitmap 的时候， 先放大 2 倍, 200x200 ([0,0][200,200])
            postScale(scale, scale)
            // 再平移 (50, 50)([50,50][250,250])
            postTranslate(dx, dy)
        }
        postCanvas.drawColor(Color.GRAY)
        val step = 50
        for (i in 0 until newWidth step step) {
            postCanvas.drawLine(i.toFloat(), 0f, i.toFloat(), newHeight.toFloat(), Paint().apply {
                color = Color.BLACK
                strokeWidth = 1f
            })
        }

        postCanvas.drawBitmap(originalBitmap, postMatrix, null)

        // Pre: 先平移后缩放，先平移后的坐标是 50 x 50，再缩放，最左上角的
        // originalBitmap 宽高是100x100 绘制 originalBitmap 的时候， 先平移 (50, 50) ([50,50][150,150])
        // 再放大 2 倍, ([100,100][300,300])
        val preCanvas = Canvas(preBitmap)
        val preMatrix = Matrix().apply {
            preScale(scale, scale)      // 后放大 2 倍
            preTranslate(dx, dy)        // 先平移 (50, 50)
        }
        preCanvas.drawColor(Color.GRAY)

        for (i in 0 until newWidth step step) {
            preCanvas.drawLine(i.toFloat(), 0f, i.toFloat(), newHeight.toFloat(), Paint().apply {
                color = Color.BLACK
                strokeWidth = 1f
            })
        }

        preCanvas.drawBitmap(originalBitmap, preMatrix, null)


        // 输出矩阵值，便于调试
        Log.e(TAG, "Post Matrix: ${postMatrix.toShortString()}")
        Log.e(TAG, "Pre Matrix: ${preMatrix.toShortString()}")


        // 显示 Post 结果（可切换为 preBitmap 查看差异）
        binding.ivOriginal.setImageBitmap(postBitmap)
        binding.ivTransformed.setImageBitmap(preBitmap)

        binding.ivOriginal.scaleType = ImageView.ScaleType.MATRIX
        binding.ivTransformed.scaleType = ImageView.ScaleType.MATRIX

    }


}
