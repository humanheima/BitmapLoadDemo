package com.hm.bitmaploadexample.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView

/**
 * Created by p_dmweidu on 2024/6/20
 * Desc: 测试 ImageView在键盘弹起时，有时候会重新布局，有时候不会重新布局的问题
 */
class TestImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ImageView(context, attrs) {


    private val TAG = "TestImageView"

    override fun requestLayout() {
        Log.d(TAG, "requestLayout: ")
        super.requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "onMeasure: ")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        Log.d(TAG, "layout: l=$l t=$t r=$r b=$b")
        super.layout(l, t, r, b)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        Log.d(TAG, "onLayout: changed=$changed left=$left top=$top right=$right bottom=$bottom width=$width height=$height")
        Log.d(TAG, Log.getStackTraceString(Throwable()))
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }
}