package com.hm.bitmaploadexample.transform

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.security.MessageDigest
import java.util.concurrent.locks.ReentrantLock

/**
 * 从顶部剪裁。参考 [TransformationUtils.centerCrop]
 *
 * 图片“高宽比”比目标视图高时，缩放图像保留顶部内容裁掉底部内容，反之，缩放裁掉左右内容
 *
 * 比如目标比例是高宽比=  3:2
 * 逻辑参考 TopCrop图片剪裁逻辑.md
 * @author wangshichao
 * @date 2024/7/18
 */
class TopCrop : BitmapTransformation() {
    companion object {
        private const val TAG = "TopCrop"
        private const val ID = "com.hm.bitmaploadexample.transform.TopCrop"
        private val ID_BYTES = ID.toByteArray(CHARSET)
        private val BITMAP_DRAWABLE_LOCK = ReentrantLock()
        private val DEFAULT_PAINT = Paint(Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)
    }


    /**
     * @param pool 从BitmapPool获取位图
     * @param inBitmap 要调整的位图，尺寸对应overrideWidth和overrideHeight，未指定以资源为准
     * @param width 最终变换后位图宽度，一般对应视图宽度
     * @param height 最终变换后位图高度，一般对应视图高度
     */
    override fun transform(
        pool: BitmapPool,
        inBitmap: Bitmap,
        width: Int,
        height: Int
    ): Bitmap {
        if (inBitmap.width == width && inBitmap.height == height) {
            Log.i(TAG, "transform: width=$width height=$height")
            return inBitmap
        }
        // From ImageView/Bitmap.createScaledBitmap.
        val scale: Float
        val dx: Float
        val dy: Float
        val matrix = Matrix()
        // height / width > inBitmap.height / inBitmap.width   时，裁掉左右内容
        if (inBitmap.width * height > width * inBitmap.height) {
            // 裁掉左右内容
            scale = height.toFloat() / inBitmap.height.toFloat()
            dx = (width - inBitmap.width * scale) * 0.5f
            dy = 0f
        } else {
            // 保留顶部内容裁掉底部内容
            scale = width.toFloat() / inBitmap.width.toFloat()
            dx = 0f
            dy = 0f
        }
        Log.i(
            TAG,
            "transform: width=$width height=$height bitmapWidth=${inBitmap.width}" +
                    " bitmapHeight=${inBitmap.height} scale=$scale dx=$dx dy=$dy"
        )

        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        val result = pool[width, height, getNonNullConfig(inBitmap)]
        applyMatrix(inBitmap, result, matrix)
        return result
    }

    override fun equals(o: Any?): Boolean {
        return o is TopCrop
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    private fun getNonNullConfig(bitmap: Bitmap): Bitmap.Config {
        return if (bitmap.config != null) bitmap.config else Bitmap.Config.ARGB_8888
    }

    private fun applyMatrix(
        inBitmap: Bitmap, targetBitmap: Bitmap, matrix: Matrix
    ) {
        BITMAP_DRAWABLE_LOCK.lock()
        try {
            val canvas = Canvas(targetBitmap)
            canvas.drawBitmap(inBitmap, matrix, DEFAULT_PAINT)
            canvas.setBitmap(null)
        } finally {
            BITMAP_DRAWABLE_LOCK.unlock()
        }
    }
}