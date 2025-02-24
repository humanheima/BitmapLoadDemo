package com.hm.bitmaploadexample.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix

object BitmapTransformer {

    /**
     * 旋转 Bitmap
     * @param source 原始 Bitmap
     * @param angle 旋转角度（度）
     * @return 旋转后的新 Bitmap
     */
    fun rotateBitmap(source: Bitmap?, angle: Float): Bitmap? {
        source ?: return null
        val matrix = Matrix().apply { postRotate(angle) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * 缩放 Bitmap
     * @param source 原始 Bitmap
     * @param scaleX X 轴缩放比例
     * @param scaleY Y 轴缩放比例
     * @return 缩放后的新 Bitmap
     */
    fun scaleBitmap(source: Bitmap?, scaleX: Float, scaleY: Float): Bitmap? {
        source ?: return null
        val matrix = Matrix().apply { postScale(scaleX, scaleY) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * 平移 Bitmap，平移要注意一下
     * @param source 原始 Bitmap
     * @param dx X 轴平移距离（像素）
     * @param dy Y 轴平移距离（像素）
     * @return 平移后的新 Bitmap
     */
    fun translateBitmap(source: Bitmap?, dx: Float, dy: Float): Bitmap? {
        source ?: return null
        val matrix = Matrix().apply { postTranslate(dx, dy) }
        val createBitmap = Bitmap.createBitmap(
            (source.width + dx).toInt(),
            (source.height + dy).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(createBitmap)
        canvas.drawBitmap(source, matrix, null)
        return createBitmap
    }

    /**
     * 倾斜 Bitmap
     * @param source 原始 Bitmap
     * @param kx X 轴倾斜系数
     * @param ky Y 轴倾斜系数
     * @return 倾斜后的新 Bitmap
     */
    fun skewBitmap(source: Bitmap?, kx: Float, ky: Float): Bitmap? {
        source ?: return null
        val matrix = Matrix().apply { postSkew(kx, ky) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * 组合变换（缩放 + 旋转）
     * @param source 原始 Bitmap
     * @param scale 缩放比例（X 和 Y 轴相同）
     * @param angle 旋转角度（度）
     * @return 变换后的新 Bitmap
     */
    fun composeTransformBitmap(source: Bitmap?, scale: Float, angle: Float): Bitmap? {
        source ?: return null
        val matrix = Matrix().apply {
            postScale(scale, scale) // 先缩放
            postRotate(angle)        // 再旋转
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}