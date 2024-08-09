package com.hm.bitmaploadexample.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.widget.EditText

/**
 * Created by p_dmweidu on 2024/6/20
 * Desc: 测试 ImageView在键盘弹起时，有时候会重新布局，有时候不会重新布局的问题
 */
class TestEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : EditText(context, attrs) {


    private val TAG = "TestEditText"

    override fun requestLayout() {
        Log.d(TAG, "requestLayout: ")
        super.requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //键盘弹起的时候，就没有走到这里
        //Log.e(TAG, "onMeasure: measureWidth = $measuredWidth measureHeight = $measuredHeight")
        //Log.e(TAG, "onMeasure: ${Log.getStackTraceString(Throwable())}")
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        //Log.d(TAG, "layout: l=$l t=$t r=$r b=$b")
        super.layout(l, t, r, b)

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        Log.d(
            TAG,
            "onLayout: changed=$changed left=$left top=$top right=$right bottom=$bottom width=$width height=$height"
        )
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        //Log.d(TAG, "setFrame: l=$l t=$t r=$r b=$b")
        //Log.d(TAG, "setFrame: ${Log.getStackTraceString(Throwable())}")
        return super.setFrame(l, t, r, b)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun requestRectangleOnScreen(rectangle: Rect?, immediate: Boolean): Boolean {
        Log.i(
            TAG,
            "requestRectangleOnScreen: rectangle = $rectangle  immediate = $immediate  ${
                Log.getStackTraceString(Exception())
            }"
        )
        return super.requestRectangleOnScreen(rectangle, immediate)
    }
}