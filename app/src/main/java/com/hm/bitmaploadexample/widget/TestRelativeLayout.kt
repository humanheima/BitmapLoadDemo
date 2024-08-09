package com.hm.bitmaploadexample.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.WindowInsets
import android.widget.RelativeLayout

/**
 * Created by p_dmweidu on 2024/7/22
 * Desc: 用来测试软键盘弹起
 */
class TestRelativeLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {


    private val TAG = "TestRelativeLayout"


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        Log.i(TAG, "onScrollChanged: l = $l  t = $t  oldl = $oldl  oldt = $oldt")
    }

    override fun onApplyWindowInsets(insets: WindowInsets?): WindowInsets {
        Log.i(TAG, "onApplyWindowInsets: insets = ${insets?.toString()}")
        return super.onApplyWindowInsets(insets)
    }



//    override fun requestRectangleOnScreen(rectangle: Rect?, immediate: Boolean): Boolean {
//        Log.i(
//            TAG,
//            "requestRectangleOnScreen: rectangle = $rectangle  immediate = $immediate  ${
//                Log.getStackTraceString(Exception())
//            }"
//        )
//        return super.requestRectangleOnScreen(rectangle, immediate)
//    }

}